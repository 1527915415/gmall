package com.atguigu.gmall.pms.service;

import com.atguigu.gamll.pms.entity.SkuAttrValueEntity;
import com.baomidou.mybatisplus.extension.service.IService;
import com.atguigu.gmall.common.bean.PageResultVo;
import com.atguigu.gmall.common.bean.PageParamVo;

import java.util.List;


/**
 * sku销售属性&值
 *
 * @author hauhau
 * @email 1527915415@qq.com
 * @date 2020-10-27 21:03:02
 */
public interface SkuAttrValueService extends IService<SkuAttrValueEntity> {

    PageResultVo queryPage(PageParamVo paramVo);

    List<SkuAttrValueEntity> querySearchSkuAttrValueBySkuIdAndCid(Long skuId, Long cid);
}

