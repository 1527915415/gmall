package com.atguigu.gmall.search.pojo;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;
import java.util.List;
@Data
@Document(indexName = "goods",type = "info",shards = 3,replicas = 2)
public class Goods {
    //商品列表字段
    @Id
    @Field(type = FieldType.Long)
    private Long skuId;
    @Field(type = FieldType.Keyword,index = false)
    private  String defaultImage;//图片
    @Field(type = FieldType.Double)
    private Double price;
    @Field(type = FieldType.Text,analyzer = "ik_max_word")
    private String title; //标题
    @Field(type = FieldType.Keyword,index = false)
    private String subTitle; //副标题
    //排序分页自字段
    @Field(type = FieldType.Long)
    private  Long sales = 0l; //销量
    @Field(type = FieldType.Date)
    private Data crateTime; //新品  商品创建时间
    @Field(type = FieldType.Boolean)
    private Boolean store = false; //库存信息
    //过滤所需字段
    @Field(type = FieldType.Long)
    private Long brandId; //品牌ID
    @Field(type = FieldType.Keyword)
    private  String brandName;
    @Field(type = FieldType.Keyword)
    private String logo;
    //分类所需字段
    @Field(type = FieldType.Long)
    private  Long categoryId; //分类id
    @Field(type = FieldType.Keyword)
    private  String categoryName; //分类名字

    @Field(type = FieldType.Nested)//嵌套字段
    private List<SearchAttrValue> searchAttrs; //搜索规格参数集合

}
