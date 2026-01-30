package com.eblog.api;

import java.util.HashMap;
import java.util.Map;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.eblog.api.common.ApiResponse;

@RestController
@RequestMapping("/api/v1")
public class HealthController {

  @GetMapping("/health")
  public ApiResponse<Map<String, Object>> health() {
    Map<String, Object> res = new HashMap<String, Object>();
    res.put("ok", Boolean.TRUE);
    return ApiResponse.ok(res);
  }
}
