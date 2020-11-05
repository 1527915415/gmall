package com.atguigu.gamll.pms.api;

import com.atguigu.gamll.pms.entity.*;
import com.atguigu.gmall.common.bean.PageParamVo;
import com.atguigu.gmall.common.bean.ResponseVo;
import io.swagger.annotations.ApiOperation;
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
}
