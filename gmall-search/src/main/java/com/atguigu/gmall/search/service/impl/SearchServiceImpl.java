package com.atguigu.gmall.search.service.impl;
import com.atguigu.gmall.pms.entity.BrandEntity;
import com.atguigu.gmall.pms.entity.CategoryEntity;
import com.atguigu.gmall.search.pojo.Goods;
import com.atguigu.gmall.search.pojo.SearchParamVo;
import com.atguigu.gmall.search.pojo.SearchResponseAttrVo;
import com.atguigu.gmall.search.pojo.SearchResponseVo;
import com.atguigu.gmall.search.service.SearchService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.lang.StringUtils;
import org.apache.lucene.search.join.ScoreMode;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.common.text.Text;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.Operator;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.index.query.RangeQueryBuilder;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;
import org.elasticsearch.search.aggregations.Aggregation;
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.search.aggregations.bucket.nested.ParsedNested;
import org.elasticsearch.search.aggregations.bucket.terms.ParsedLongTerms;
import org.elasticsearch.search.aggregations.bucket.terms.ParsedStringTerms;
import org.elasticsearch.search.aggregations.bucket.terms.Terms;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightBuilder;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightField;
import org.elasticsearch.search.sort.SortOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class SearchServiceImpl implements SearchService {
    @Autowired //注册客户端
    private RestHighLevelClient restHighLevelClient;
    //序列化
    private static final ObjectMapper MAPPER = new ObjectMapper();
    @Override
    //查询方法    paramVo 查询参数对象
    //http://localhost:18086/search?keyword=%E6%89%8B%E6%9C%BA&brandId=2,3&categoryId=225&priceFrom=1000&priceTo=4000&store=false&props=4:8G-6G
    //http://localhost:18086/search?keyword=%E6%89%8B%E6%9C%BA&brandId=2,3&categoryId=225&priceFrom=1000&priceTo=4000&store=false&props=4:8G-12G&sort=1&pageNum=2
    public SearchResponseVo search(SearchParamVo paramVo) {
        try {
            //构建索引库,和搜索条件 即Dls语句
            SearchRequest searchRequest = new SearchRequest(new String[]{"goods"}, buildDsl(paramVo));
            //从el中查询出来的结果集
            System.out.println(paramVo);
            SearchResponse response = this.restHighLevelClient.search(searchRequest, RequestOptions.DEFAULT);
            System.out.println("========= " + response);
            //把从el中查询出来的结果集 ,前端所需要的结果集
            SearchResponseVo responseVo = this.parseSearchResult(response);
            responseVo.setPageNum(paramVo.getPageNum());
            responseVo.setPageSize(paramVo.getPageSize());
            System.out.println(responseVo);
            return responseVo;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }
    //解析结果搜索集方法
    private SearchResponseVo parseSearchResult( SearchResponse response){
        SearchResponseVo responseVo = new SearchResponseVo();
        //解析hits
        SearchHits hits = response.getHits();
        //总记录数
        responseVo.setTotal(hits.getTotalHits());
        //当前页记录数
        SearchHit[] hitsHits = hits.getHits();
        if (hitsHits == null || hitsHits.length == 0){
            return null;
        }
        //把hitsHits 转化成集合
       // Arrays.stream(hitsHits).map(hitsHits ->{});
        List<Goods> goodsList = Arrays.stream(hitsHits).map(hitsHit -> {
            try {
                String json = hitsHit.getSourceAsString();
                //对象反序列化
                //JSONObject goods = JSON.parseObject(json);
                Goods goods = MAPPER.readValue(json, Goods.class);

                //获取高亮字段  奢姿给goods对象
                Map<String, HighlightField> highlightFields = hitsHit.getHighlightFields();
                HighlightField highlightField = highlightFields.get("title");
                Text[] fragments = highlightField.getFragments();
                    goods.setTitle(fragments[0].string());
                return goods;
            } catch (JsonProcessingException e) {
                e.printStackTrace();
            }
            return null;
        }).collect(Collectors.toList());
        responseVo.setGoodsList(goodsList);

        //解析aggregations
        Map<String, Aggregation> aggregationMap = response.getAggregations().asMap();
        //解析品牌聚合结果集

        ParsedLongTerms brandIgAgg =(ParsedLongTerms) aggregationMap.get("brandIdAgs");

        List<? extends Terms.Bucket> brandBuckets = brandIgAgg.getBuckets();
        if (!CollectionUtils.isEmpty(brandBuckets)){
            responseVo.setBrands(brandBuckets.stream().map(bucket ->{
                BrandEntity brandEntity = new BrandEntity();
                //获取attrIgAgg中的key ,这个key就是品牌的Id
                brandEntity.setId(((Terms.Bucket) bucket).getKeyAsNumber().longValue());
                //品牌子聚合的map结构
                Map<String, Aggregation> subAggregationMap = ((Terms.Bucket) bucket).getAggregations().asMap();
                //获取品牌名称子聚合
                ParsedStringTerms brandNameAgg = (ParsedStringTerms)subAggregationMap.get("brandNameAge");
                List<? extends Terms.Bucket> nameAggBuckets = brandNameAgg.getBuckets();
                //获取商品中的第一个元素
                if (!CollectionUtils.isEmpty(nameAggBuckets)){
                    brandEntity.setName(nameAggBuckets.get(0).getKeyAsString());
                }
                //获取logo子聚合
                ParsedStringTerms logoAgg = (ParsedStringTerms)subAggregationMap.get("logoAgg");
                //获取logo中的桶
                List<? extends Terms.Bucket> logoAggBuckets = logoAgg.getBuckets();
                if (!CollectionUtils.isEmpty(logoAggBuckets)){

                    brandEntity.setLogo( logoAggBuckets.get(0).getKeyAsString());
                }
                return brandEntity;
            }).collect(Collectors.toList()));
        }
        //解析分类聚合结果集
        ParsedLongTerms categoryIdAgg = (ParsedLongTerms)aggregationMap.get("categoryIdAgg");
        List<? extends Terms.Bucket> categoryBuckets = categoryIdAgg.getBuckets();
        if (CollectionUtils.isEmpty(categoryBuckets)){

            responseVo.setCategories( categoryBuckets.stream().map(categoryBucket ->{
                CategoryEntity categoryEntity = new CategoryEntity();

                //获取分类id
                categoryEntity.setId(((Terms.Bucket) categoryBucket).getKeyAsNumber().longValue());
                //获取分类名称
                ParsedStringTerms categoryNameAgg = (ParsedStringTerms)((Terms.Bucket) categoryBucket).getAggregations().get("categoryNameAgg");
                List<? extends Terms.Bucket> nameAggBuckets = categoryNameAgg.getBuckets();
                if (!CollectionUtils.isEmpty(nameAggBuckets)){
                    categoryEntity.setName(nameAggBuckets.get(0).getKeyAsString());
                }
                return categoryEntity;
            }).collect(Collectors.toList()));// 分类
        }
        //解析规格参数聚合结果集
        ParsedNested attrAgg =(ParsedNested) aggregationMap.get("attrAgg");
        //获取嵌套集合中的子集合  attrIdAgg
        ParsedLongTerms attrIdAgg = (ParsedLongTerms)attrAgg.getAggregations().get("attrIdAgg");
        //获取id子集合中的桶
        List<? extends Terms.Bucket> attrIdAggBuckets = attrIdAgg.getBuckets();
        if (!CollectionUtils.isEmpty(attrIdAggBuckets)){
            //把桶集合转化成 SearchResponseAttrVo 集合
            List<SearchResponseAttrVo> filters = attrIdAggBuckets.stream().map(brandBucket ->{
                SearchResponseAttrVo responseAttrVo = new SearchResponseAttrVo();
                //获取规格参数id
                responseAttrVo.setAttrId(((Terms.Bucket) brandBucket).getKeyAsNumber().longValue());
                //获取id 聚合中的所有子聚合
                Map<String, Aggregation> supAggregationMap = ((Terms.Bucket) brandBucket).getAggregations().asMap();
                //获取name的子聚合
                ParsedStringTerms attrNameAgg = (ParsedStringTerms)supAggregationMap.get("attrNameAgg");
                //获取name的值集合
                List<? extends Terms.Bucket> nameAggBuckets = attrNameAgg.getBuckets();
                if (!CollectionUtils.isEmpty(nameAggBuckets)){
                    responseAttrVo.setAttrName(nameAggBuckets.get(0).getKeyAsString());
                }
                //获取规格参数值 attrValueAgg
                ParsedStringTerms attrValueAgg = (ParsedStringTerms)supAggregationMap.get("attrValueAgg");
                List<? extends Terms.Bucket> buckets = attrValueAgg.getBuckets();
                if (!CollectionUtils.isEmpty(buckets)){
                    responseAttrVo.setAttrValues( buckets.stream().map( Terms.Bucket::getKeyAsString).collect(Collectors.toList()));
                }
                return responseAttrVo;
            }).collect(Collectors.toList());

            responseVo.setFilters(filters);// 规格参数
        }
        //System.out.println(responseVo);
        return responseVo;

    }
    //构建Dls 搜索语句
    private SearchSourceBuilder buildDsl (SearchParamVo paramVo){
        //构建Dlo语句
        SearchSourceBuilder sourceBuilder = new SearchSourceBuilder();
        //获取查询的关键字
        String keyword = paramVo.getKeyword();
        if (StringUtils.isBlank(keyword)){
            //打广告
            return sourceBuilder;
        }
        //1 .构建查询条件及过滤条件  query
        BoolQueryBuilder boolQueryBuilder = QueryBuilders.boolQuery();
        sourceBuilder.query(boolQueryBuilder);
        //1.1 构建匹配查询  must

        boolQueryBuilder.must(QueryBuilders.matchQuery("title",keyword).operator(Operator.AND));
        //1.2构建过滤条件  filter
        //1.2.1 构建品牌过滤 brandId
        List<Long> brandId = paramVo.getBrandId();
        if (!CollectionUtils.isEmpty(brandId)){
            boolQueryBuilder.filter(QueryBuilders.termsQuery("brandId",brandId));
        }
        //1.2.2 构建分类过滤  categoryId
        List<String> categoryId = paramVo.getCategoryId();
        if (!CollectionUtils.isEmpty(categoryId)){
            boolQueryBuilder.filter(QueryBuilders.termsQuery("categoryId",categoryId));
        }
        //1.2.3 构建价格区间多虑  range ->price
        Double priceFrom = paramVo.getPriceFrom();
        Double priceTo = paramVo.getPriceTo();
        if(priceFrom != null || priceTo != null){
            RangeQueryBuilder queryBuilder = QueryBuilders.rangeQuery("price");
            boolQueryBuilder.filter(queryBuilder);
            if (priceFrom != null){
                queryBuilder.gte(priceFrom);
            }
            if (priceTo != null){
                queryBuilder.lte(priceTo);
            }
        }
        //1.2.4构建是否有货过滤  term ->store
        Boolean store = paramVo.getStore();
        if (store != null){
            boolQueryBuilder.filter(QueryBuilders.termQuery("store",store));
        }
        //1.25 构建规格参数嵌套过滤  nested
        List<String> props = paramVo.getProps();
        if (!CollectionUtils.isEmpty(props)){
            //4:8G-12G
            props.forEach(prop ->{
                //字符串分割
                String[] attrs = StringUtils.split(prop, ":");
                if (attrs != null && attrs.length == 2){
                    BoolQueryBuilder boolQuery = QueryBuilders.boolQuery();
                    String attrValue = attrs[0];
                    boolQuery.must(QueryBuilders.termQuery("searchAttrs.attrId",attrValue));
                    String attr = attrs[1];
                    String[] split = StringUtils.split(attr, "-");
                    boolQuery.must(QueryBuilders.termsQuery("searchAttrs.attrValue",split));
                    //nestedQuery() 嵌套查询    ScoreMode.None 不需要的分模式
                    boolQueryBuilder.filter(QueryBuilders.nestedQuery("searchAttrs",boolQuery, ScoreMode.None));
                }
            });
        }
        //2.构建排序条件  sort  1-价格升序 2-价格升序 3=销量降序 4 新品降序   0 默认得分排序
        Integer sort = paramVo.getSort();
        if (sort != null){
            switch (sort){
                case 1: sourceBuilder.sort("price", SortOrder.ASC); break;
                case 2:sourceBuilder.sort("price",SortOrder.DESC);break;
                case 3:sourceBuilder.sort("sales",SortOrder.DESC);break;
                case 4:sourceBuilder.sort("createTime",SortOrder.DESC);break;
                default:sourceBuilder.sort("_score",SortOrder.DESC);
                    break;
            }
        }

        //3. 构建分页条件  from size
        Integer pageSize = paramVo.getPageSize();
        Integer pageNum = paramVo.getPageNum();
        sourceBuilder.from((pageNum -1) * pageSize);
        sourceBuilder.size(pageSize);
        //4. 构建高亮   highlight  "<font style-'color:red;'"  "</font>"
        sourceBuilder.highlighter(new HighlightBuilder()
                .field("title")
                .preTags("<font style-'color:red;'")
                .postTags("</font>"));
        //5. 构建聚合查询  aggs
        //5.1 构建品牌聚合  brandIdAgs
        sourceBuilder.aggregation(AggregationBuilders.terms("brandIdAgs").field("brandId")
                .subAggregation(AggregationBuilders.terms("brandNameAge").field("brandName"))
                .subAggregation(AggregationBuilders.terms("logoAgg").field("logo")));
        //5.2 构建分类聚合  categoryIdAgg
        sourceBuilder.aggregation(AggregationBuilders.terms("categoryIdAgg").field("categoryId")
                .subAggregation(AggregationBuilders.terms("categoryNameAgg").field("categoryName")));
        //5.3 构建规格参数聚合  attrAgg
        sourceBuilder.aggregation(AggregationBuilders.nested("attrAgg","searchAttrs")
                .subAggregation(AggregationBuilders.terms("attrIdAgg").field("searchAttrs.attrId")
                        .subAggregation(AggregationBuilders.terms("attrNameAgg").field("searchAttrs.attrName"))
                        .subAggregation(AggregationBuilders.terms("attrValueAgg").field("searchAttrs.attrValue"))));
        //6.0 添加结果集过滤
        sourceBuilder.fetchSource(new String[]{"skuId","title","subTitle","defaultImage","price"},null);
        System.out.println(sourceBuilder);
        return sourceBuilder;
    }
}
