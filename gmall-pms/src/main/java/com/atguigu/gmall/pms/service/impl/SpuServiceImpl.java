package com.atguigu.gmall.pms.service.impl;

import com.atguigu.gmall.pms.entity.*;
import com.atguigu.gmall.pms.feign.GmallSmsClient;
import com.atguigu.gmall.pms.mapper.SkuMapper;
import com.atguigu.gmall.pms.mapper.SpuDescMapper;
import com.atguigu.gmall.pms.service.*;
import com.atguigu.gmall.pms.vo.SkuVo;
import com.atguigu.gmall.pms.vo.SupAttrValueVo;
import com.atguigu.gmall.pms.vo.SupVo;
import com.atguigu.gmall.sms.vo.SkuSaleVo;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.atguigu.gmall.common.bean.PageResultVo;
import com.atguigu.gmall.common.bean.PageParamVo;

import com.atguigu.gmall.pms.mapper.SpuMapper;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;


@Service("spuService")
public class SpuServiceImpl extends ServiceImpl<SpuMapper, SpuEntity> implements SpuService {

    @Override
    public PageResultVo queryPage(PageParamVo paramVo) {
        IPage<SpuEntity> page = this.page(
                paramVo.getPage(),
                new QueryWrapper<SpuEntity>()
        );

        return new PageResultVo(page);
    }

    @Override
    public PageResultVo querySupInfo(PageParamVo pageParamVo, Long categoryId) {
        //封装查询条件
        QueryWrapper<SpuEntity> wrapper = new QueryWrapper<>();
        //categoryId 不为零查询全部
        if (categoryId != 0){
            wrapper.eq("category_id",categoryId);
        }
        //搜索条件
        String key = pageParamVo.getKey();
        //isNotBlank(key) 该方法出了判断null 空  还有空格
        //isNotEmpty() 只判断null 和空 , 不判断空格
        if (StringUtils.isNotBlank(key)){
            //SELECT *
            //FROM pms_spu
            //WHERE category_id = 225
            //AND (`name` LIKE '%7%' OR id = '7')
            //and()可以拼接()
            wrapper.and(t -> t.like("name",key).or().like("id",key));
        }
        //分页查询 .getPage() 获取分页参数
        return new PageResultVo(page(pageParamVo.getPage(),wrapper));
    }

    @Autowired
    private SpuDescService spuDescSrevice;
    @Autowired
    private SpuAttrValueService spuAttrValueService;
    @Autowired
    private SkuMapper skuMapper;
    @Autowired
    private SkuImagesService skuImagesService;
    @Autowired
    private SkuAttrValueService skuAttrValueService;
    @Autowired
    private GmallSmsClient gmallSmsClient;
    //指定ArithmeticException异常 发生时 不用事务回滚  运行yic
    //指定FileNotFoundException 异常 发生时事务回滚   FileNotFoundException是首检异常
    //@Transactional 所有的受检异常（编译时）都不回滚
    //	所有的不受检异常（运行时异常）都会回滚
    @Transactional(noRollbackFor = ArithmeticException.class,rollbackFor = FileNotFoundException.class)
    @Override
    public void bigSave(SupVo spu) {
        //1.保存sup相关
        Long spuId = saveSpu(spu);
        //1.2 保存spu_desc表s
      this.spuDescSrevice.saveSpuDesc(spu,spuId);
        //int a = 1/0;
        //new FileInputStream("xxxxx");
        //1.3 保存spu_attr_value表
        saveBaseAttr(spu, spuId);
        //2. 保存sku相关信息
        saveSkus(spu, spuId);

    }

    private void saveSkus(SupVo spu, Long spuId) {
        List<SkuVo> skus = spu.getSkus();
        if (CollectionUtils.isEmpty(skus)){
            return;
        }
        skus.forEach(skuVo -> {
            //2.1 保存sku表
            skuVo.setSpuId(spuId);
            skuVo.setBrandId(spu.getBrandId());
            skuVo.setCatagoryId(spu.getCategoryId());
            //默认图片取第一张
            List<String> images = skuVo.getImages();
            if(!CollectionUtils.isEmpty(images)){
                skuVo.setDefaultImage(StringUtils.isNotBlank(skuVo.getDefaultImage()) ? skuVo.getDefaultImage() : images.get(0));
            }
            this.skuMapper.insert(skuVo);
            Long skuId = skuVo.getId();
            //2.2 保存sku_images图片表
            if(!CollectionUtils.isEmpty(images)){

                this.skuImagesService.saveBatch(images.stream().map(image -> {
                    SkuImagesEntity skuImagesEntity = new SkuImagesEntity();
                    skuImagesEntity.setSkuId(skuId);
                    skuImagesEntity.setUrl(image);
                    //默认图片 1 是默认图片
                    skuImagesEntity.setDefaultStatus(StringUtils.equals(image,skuVo.getDefaultImage()) ? 1 : 0);
                    return skuImagesEntity;
                }).collect(Collectors.toList()));
            }
            //2.3 保存sku_attr_value 表
            List<SkuAttrValueEntity> saleAttrs = skuVo.getSaleAttrs();
            if (!CollectionUtils.isEmpty(saleAttrs)){
                saleAttrs.forEach(skuAttrValueEntity -> skuAttrValueEntity.setSkuId(skuId));
                this.skuAttrValueService.saveBatch(saleAttrs);
            }

            //3. 保存sku的营销信息 ，需要远程调用gmall-sms
            SkuSaleVo skuSaleVo = new SkuSaleVo();
            BeanUtils.copyProperties(skuVo,skuSaleVo);
            skuSaleVo.setSkuId(skuVo.getId());
            this.gmallSmsClient.saveSales(skuSaleVo);
            //3.1 保存sku_bounds表
            //3.2 保存sku_full_reduction表
            //3.3 保存sku_ladder表 打折信息
        });
    }

    private void saveBaseAttr(SupVo spu, Long spuId) {
        List<SupAttrValueVo> baseAttrs = spu.getBaseAttrs();
        if (!CollectionUtils.isEmpty(baseAttrs)) {
            //把SupAttrValueVo对象,复制到spuAttrValueEntity中,再转换成spuAttrValueEntities集合
            //map() 可以实现对象属性赋值,返回值是 stream 流
            List<SpuAttrValueEntity> spuAttrValueEntities = baseAttrs.stream().map(supAttrValueVo -> {
                SpuAttrValueEntity spuAttrValueEntity = new SpuAttrValueEntity();
                //把supAttrValueVo 中的值,复制到spuAttrValueEntity中
                BeanUtils.copyProperties(supAttrValueVo, spuAttrValueEntity);
                //设置spu_id
                spuAttrValueEntity.setSpuId(spuId);
                return spuAttrValueEntity;
            }).collect(Collectors.toList());
            //saveBatch()方法 Service 有
            this.spuAttrValueService.saveBatch(spuAttrValueEntities);
        }
    }


    private Long saveSpu(SupVo spu) {
        // 1.0 保存spu基本信息 spu 表
        spu.setCreateTime(new Date());
        spu.setUpdateTime(spu.getCreateTime());
        this.save(spu);
        return spu.getId();
    }
}