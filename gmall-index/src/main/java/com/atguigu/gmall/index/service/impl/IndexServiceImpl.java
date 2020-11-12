package com.atguigu.gmall.index.service.impl;

import com.alibaba.fastjson.JSON;
import com.atguigu.gmall.pms.entity.CategoryEntity;
import com.atguigu.gmall.common.bean.ResponseVo;
import com.atguigu.gmall.index.feign.GmallPmsClient;
import com.atguigu.gmall.index.service.IndexService;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Service
public class IndexServiceImpl implements IndexService {
    @Autowired
    private GmallPmsClient gmallPmsClient;
    @Autowired
    private StringRedisTemplate stringRedisTemplate;
    public static  final String KET_PREFIX = "index:category:";
    @Override
    public List<CategoryEntity> queryLvl1Categories() {
        ResponseVo<List<CategoryEntity>> listResponseVo = this.gmallPmsClient.queryCategory(0l);
        List<CategoryEntity> data = listResponseVo.getData();
        return data;
    }

    @Override
    public  void testLock() {
      //1.0 从Redis中获取锁
        String uuid = UUID.randomUUID().toString();
        Boolean lock = this.stringRedisTemplate.opsForValue().setIfAbsent("lock", uuid,3,TimeUnit.SECONDS);
        System.out.println("=========================");
        if (lock){
            //查询redis中的值
            String value = this.stringRedisTemplate.opsForValue().get("num");
            //没有改值就return
            if (StringUtils.isBlank(value)){
                return;
            }
            //有值就转换成int
            int num = Integer.parseInt(value);
            //把redis 中的值加一
            this.stringRedisTemplate.opsForValue().set("num",String.valueOf(++num));

            //2.0 释放锁
//            if (StringUtils.equals(stringRedisTemplate.opsForValue().get("look"),uuid)){
//                this.stringRedisTemplate.delete("lock");
//            }
            //使用LUA 脚本释放锁,保证代码的原子性
            //              "if redis.call('get', KEYS[1]) == ARGV[1] then return redis.call('del', KEYS[1]) else return 0 end"
            String script = "if redis.call('get', KEYS[1]) == ARGV[1] then return redis.call('del', KEYS[1]) else return 0 end";
            Boolean lock1 = this.stringRedisTemplate.execute(new DefaultRedisScript<>(script, Boolean.class), Arrays.asList("lock"), uuid);
        }else {
            //3.0 每个一秒回调一次,尝试获取锁
            try {
                Thread.sleep(1000);
                testLock();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public List<CategoryEntity> queryLvl2CategoriesWithSub(Long pId) {
        //先从Redis中查取
        String categoryEntitys = this.stringRedisTemplate.opsForValue().get(KET_PREFIX + pId);
        if (StringUtils.isNotBlank(categoryEntitys)){
            //Redis中有,直接返回
            List<CategoryEntity> categoryEntityList = JSON.parseArray(categoryEntitys, CategoryEntity.class);
            return categoryEntityList;
        }
        //Redis中没有,到数据库去查,并往Redis中保存一份
        ResponseVo<List<CategoryEntity>> listResponseVo = this.gmallPmsClient.queryLvl2CatesWithsubByPid(pId);
        this.stringRedisTemplate.opsForValue().set(KET_PREFIX + pId,JSON.toJSONString(listResponseVo),30, TimeUnit.DAYS);

        return listResponseVo.getData();
    }
}
