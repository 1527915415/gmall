package com.atguigu.gmall.oms.mapper;

import com.atguigu.gmall.oms.entity.OrderEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * 订单
 * 
 * @author hauhau
 * @email 1527915415@qq.com
 * @date 2020-10-27 21:11:06
 */
@Mapper
public interface OrderMapper extends BaseMapper<OrderEntity> {
	
}
