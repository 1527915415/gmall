package com.atguigu.gmall.ums.mapper;

import com.atguigu.gmall.ums.entity.UserEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * 用户表
 * 
 * @author hauhau
 * @email 1527915415@qq.com
 * @date 2020-10-27 21:21:15
 */
@Mapper
public interface UserMapper extends BaseMapper<UserEntity> {
	
}
