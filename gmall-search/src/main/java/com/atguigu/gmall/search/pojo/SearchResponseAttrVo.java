package com.atguigu.gmall.search.pojo;

import lombok.Data;

import java.util.List;

/**
 * 参数属性集合模型
 */
@Data
public class SearchResponseAttrVo {
    private Long attrId;
    private String attrName;
    private List<String> attrValues;
}
