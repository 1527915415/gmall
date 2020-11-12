package com.atguigu.gmall.pms.api;

import com.atguigu.gmall.pms.entity.*;
import com.atguigu.gmall.common.bean.PageParamVo;
import com.atguigu.gmall.common.bean.ResponseVo;
import org.springframework.web.bind.annotation.*;

import java.util.List;
public interface GmallPmsApi {
    @PostMapping("pms/spu/json")
    public ResponseVo<List<SpuEntity>> querySpuByPageJson(@RequestBody PageParamVo paramVo);
    @GetMapping("pms/sku/spu/{spuId}")
    public ResponseVo<List<SkuEntity>> querySkuBySupId(@PathVariable("spuId") Long spuId);
    @GetMapping("pms/brand/{id}")
    public ResponseVo<BrandEntity> queryBrandById(@PathVariable("id") Long id);
    @GetMapping("pms/category/{id}")
    public ResponseVo<CategoryEntity> queryCategoryById(@PathVariable("id") Long id);
    @GetMapping("pms/category/parent/{parentId}")
    public ResponseVo<List<CategoryEntity>> queryCategory(@PathVariable("parentId") Long parentId );
    @GetMapping("pms/skuattrvalue/search/attr/{skuId}")
    public ResponseVo<List<SkuAttrValueEntity>> querySearchSkuAttrValueBySkuIdAndCid(
            @PathVariable("skuId") Long skuId,
            @RequestParam("Cid") Long cid
    );
    @GetMapping("pms/spuattrvalue/search/attr/{spuId}")
    public ResponseVo<List<SpuAttrValueEntity>> querySearchSpuAttrValueBySpuAndCid(
            @PathVariable("spuId") Long spuId,
            @RequestParam("Cid") Long Cid
    );
    @GetMapping("pms/spu/{id}")
    public ResponseVo<SpuEntity> querySpuById(@PathVariable("id") Long id);
    @GetMapping("pms/category/parent/sub/{parentId}")
    public ResponseVo<List<CategoryEntity>>queryLvl2CatesWithsubByPid(@PathVariable("parentId") Long pid);
}
