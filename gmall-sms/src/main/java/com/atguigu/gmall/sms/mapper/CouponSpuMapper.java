package com.atguigu.gmall.sms.mapper;

import com.atguigu.gmall.sms.entity.CouponSpuEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * 优惠券与产品关联
 * 
 * @author hauhau
 * @email 1527915415@qq.com
 * @date 2020-10-27 21:15:17
 */
@Mapper
public interface CouponSpuMapper extends BaseMapper<CouponSpuEntity> {
	
}
