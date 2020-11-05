package com.atguigu.gmall.pms.vo;
import com.atguigu.gamll.pms.entity.SpuEntity;
import lombok.Data;

import java.util.List;

@Data
//spuInfo扩展对象
// * 包含：spuInfo基本信息、spuImages图片信息、baseAttrs基本属性信息、skus信息
public class SupVo extends SpuEntity {
    //图片信息
    private List<String> spuImages;
    //基本属信息
    private List<SupAttrValueVo> baseAttrs;
    //sku 信息
    private List<SkuVo> skus;
}
