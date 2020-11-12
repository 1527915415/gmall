package com.atguigu.gmall.search.listener;

import com.atguigu.gmall.pms.entity.*;
import com.atguigu.gmall.common.bean.ResponseVo;
import com.atguigu.gmall.search.feign.GmallPmsClient;
import com.atguigu.gmall.search.feign.GmallWmsClient;
import com.atguigu.gmall.search.pojo.Goods;
import com.atguigu.gmall.search.pojo.SearchAttrValue;
import com.atguigu.gmall.search.repository.GoodsRepository;
import com.atguigu.gmall.search.service.SearchService;
import com.atguigu.gmall.wms.entity.WareSkuEntity;

import com.rabbitmq.client.Channel;
import org.springframework.amqp.core.ExchangeTypes;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.Exchange;
import org.springframework.amqp.rabbit.annotation.Queue;
import org.springframework.amqp.rabbit.annotation.QueueBinding;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;


import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class SpuInfoListener {
    @Autowired
    private SearchService searchService;
    @Autowired
    private GmallPmsClient pmsClient;
    @Autowired
    private GmallWmsClient wmsClient;
    @Autowired
    private GoodsRepository goodsRepository;
    @RabbitListener(bindings = @QueueBinding(
            value = @Queue(value = "SEARCH_SAVE_QUEUE",durable = "true"),
            exchange = @Exchange(
                    value = "PMS_SPU_EXCHANGE",
                    ignoreDeclarationExceptions = "true",
                    type = ExchangeTypes.TOPIC
            ),
            key = {"item.insert"}
    ))
    public void  listenerCreate(Long spuId, Channel channel, Message message) throws IOException {
        //查询sku
        ResponseVo<List<SkuEntity>> skuResponseVo = this.pmsClient.querySkuBySupId(spuId);
        List<SkuEntity> skuEntityList = skuResponseVo.getData();
        if (!CollectionUtils.isEmpty(skuEntityList)){
            List<Goods> goodsList = skuEntityList.stream().map(skuEntity -> {
                Goods goods = new Goods();
                //sku相关信息设置进来
                goods.setSkuId(skuEntity.getId());
                goods.setTitle(skuEntity.getTitle());
                goods.setSubTitle(skuEntity.getSubtitle());
                //类型转换
                goods.setPrice(skuEntity.getPrice().doubleValue());
                goods.setDefaultImage(skuEntity.getDefaultImage());
                //spu的创建时间
                ResponseVo<SpuEntity> spuEntityResponseVo = this.pmsClient.querySpuById(spuId);
                SpuEntity spuEntity = spuEntityResponseVo.getData();
                if (spuEntity != null){
                    goods.setCrateTime(spuEntity.getCreateTime());
                }
                //查询库存相关信息
                ResponseVo<List<WareSkuEntity>> wareResponseVo = this.wmsClient.queryByShuId(skuEntity.getId());
                List<WareSkuEntity> wereSkuEntitys = wareResponseVo.getData();
                if (!CollectionUtils.isEmpty(wereSkuEntitys)){
                    //任意一个仓库有库存 - 锁定库存 > 0 就比奥斯有货
                    goods.setStore(wereSkuEntitys.stream().
                            anyMatch(wareSkuEntity -> wareSkuEntity.getStock() - wareSkuEntity.getStockLocked() > 0));
                    goods.setSales(wereSkuEntitys.stream().map(WareSkuEntity::getSales).reduce((a,b) -> a + b).get());
                }
                //查询品牌
                ResponseVo<BrandEntity> brandEntityResponseVo = this.pmsClient.queryBrandById(skuEntity.getBrandId());
                BrandEntity brandEntity = brandEntityResponseVo.getData();
                if (brandEntity != null){
                    goods.setBrandId(brandEntity.getId());
                    goods.setBrandName(brandEntity.getName());
                    goods.setLogo(brandEntity.getLogo());
                }
                //查询分类
                ResponseVo<CategoryEntity> categoryEntityResponseVo = this.pmsClient.queryCategoryById(skuEntity.getCatagoryId());
                CategoryEntity categoryEntity = categoryEntityResponseVo.getData();
                if (categoryEntity != null){
                    goods.setCategoryId(categoryEntity.getId());
                    goods.setCategoryName(categoryEntity.getName());
                }
                //查询规格参数
                List<SearchAttrValue> searchAttrValues = new ArrayList<>();
                //销售类型的检索参规格及值
                ResponseVo<List<SkuAttrValueEntity>> skuAttrValueResponseVo = this.pmsClient.
                        querySearchSkuAttrValueBySkuIdAndCid(skuEntity.getId(), skuEntity.getCatagoryId());
                List<SkuAttrValueEntity> skuAttrValueEntities = skuAttrValueResponseVo.getData();
                if (!CollectionUtils.isEmpty(skuAttrValueEntities)){
                    searchAttrValues.addAll(skuAttrValueEntities.stream().map(skuAttrValueEntity -> {
                        SearchAttrValue searchAttrValue = new SearchAttrValue();
                        BeanUtils.copyProperties(skuAttrValueEntity,searchAttrValue);
                        return searchAttrValue;
                    }).collect(Collectors.toList()));
                }

                // 基本类型的检索规格参数
                ResponseVo<List<SpuAttrValueEntity>> spuAttrValueResponseVo = this.pmsClient.
                        querySearchSpuAttrValueBySpuAndCid(skuEntity.getSpuId(), skuEntity.getCatagoryId());
                List<SpuAttrValueEntity> spuAttrValueEntities = spuAttrValueResponseVo.getData();
                if (!CollectionUtils.isEmpty(spuAttrValueEntities)){
                    searchAttrValues.addAll(   spuAttrValueEntities.stream().map(spuAttrValueEntity -> {
                        SearchAttrValue searchAttrValue = new SearchAttrValue();
                        BeanUtils.copyProperties(spuAttrValueEntity,searchAttrValue);
                        return searchAttrValue;
                    }).collect(Collectors.toList()));
                }

                goods.setSearchAttrs(searchAttrValues);
                return goods;
            }).collect(Collectors.toList());
            this.goodsRepository.saveAll(goodsList);
        }
        try {
            //手动确认消息
            channel.basicAck(message.getMessageProperties().getDeliveryTag(),false);
        } catch (IOException e) {
            e.printStackTrace();
            //是否重试过
            if (message.getMessageProperties().getRedelivered()){
                //已经重试过,直接决绝
                channel.basicNack(message.getMessageProperties().getDeliveryTag(),false,false);
            }else {
                //没有重试.重新归队
                channel.basicReject(message.getMessageProperties().getDeliveryTag(),false);
            }
        }
    }
}
