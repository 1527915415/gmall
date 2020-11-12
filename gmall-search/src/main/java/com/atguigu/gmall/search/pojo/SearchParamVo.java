package com.atguigu.gmall.search.pojo;

import lombok.Data;

import java.util.List;

/**
 * 前端闯入的参数对象
 */
@Data
public class SearchParamVo {
    //过滤参数
    private  String keyword; //搜索条件
    private List<Long> brandId;//品牌过滤ID
    private List<String> categoryId; //分类过滤的条件
    private List<String> props;//规格参数的过滤
    //排序参数
    private Integer sort = 0; //默认排序
    private Double priceFrom; //价格区间
    private Double priceTo;

    private Integer pageNum = 1; // 页码
    private final Integer pageSize = 20; //每页记录数

    private  Boolean store; //是f否有货

}
