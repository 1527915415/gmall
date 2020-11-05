package com.atguigu.gmall.search.feign;

import com.atguigu.gamll.pms.api.GmallPmsApi;
import org.springframework.cloud.openfeign.FeignClient;

@FeignClient(value = "pms-service" )
public interface GmallPmsClient extends GmallPmsApi {
}
