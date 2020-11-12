package com.atguigu.gmall.pms.service;

import com.atguigu.gmall.pms.entity.SpuDescEntity;
import com.atguigu.gmall.pms.vo.SupVo;
import com.baomidou.mybatisplus.extension.service.IService;
import com.atguigu.gmall.common.bean.PageResultVo;
import com.atguigu.gmall.common.bean.PageParamVo;


/**
 * spu信息介绍
 *
 * @author hauhau
 * @email 1527915415@qq.com
 * @date 2020-10-27 21:03:02
 */
public interface SpuDescService extends IService<SpuDescEntity> {

    PageResultVo queryPage(PageParamVo paramVo);
    void saveSpuDesc(SupVo spu,Long spuId);
}

