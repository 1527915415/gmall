package com.atguigu.gmall.pms.feign;
import com.atguigu.gmall.sms.api.GamllSmsApi;
import org.springframework.cloud.openfeign.FeignClient;
@FeignClient("sms-service")
public interface GmallSmsClient extends GamllSmsApi {

}
