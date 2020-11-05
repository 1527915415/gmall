package com.atguigu.gmall.pms.vo;

import com.alibaba.nacos.client.naming.utils.CollectionUtils;
import com.atguigu.gamll.pms.entity.SpuAttrValueEntity;
import org.apache.commons.lang.StringUtils;

import java.util.List;
public class SupAttrValueVo extends SpuAttrValueEntity {
    //重写SpuAttrValueEntity  的setValueSelected方法
    public void setValueSelected(List<Object> valueSelected){
        //如果接受的集合为空,则不设置
        if (CollectionUtils.isEmpty(valueSelected)){
            return;
        }
        this.setAttrValue(StringUtils.join(valueSelected,","));
    }
}
