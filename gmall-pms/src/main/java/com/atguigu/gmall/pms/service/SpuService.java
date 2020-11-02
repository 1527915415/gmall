package com.atguigu.gmall.pms.service;

import com.atguigu.gmall.pms.vo.SupVo;
import com.baomidou.mybatisplus.extension.service.IService;
import com.atguigu.gmall.common.bean.PageResultVo;
import com.atguigu.gmall.common.bean.PageParamVo;
import com.atguigu.gmall.pms.entity.SpuEntity;

import java.util.Map;

/**
 * spu信息
 *
 * @author hauhau
 * @email 1527915415@qq.com
 * @date 2020-10-27 21:03:02
 */
public interface SpuService extends IService<SpuEntity> {

    PageResultVo queryPage(PageParamVo paramVo);

    PageResultVo querySupInfo(PageParamVo pageParamVo, Long categoryId);

    void bigSave(SupVo spu);
}

