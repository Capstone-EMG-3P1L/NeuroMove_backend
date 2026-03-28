package com.neuromove.backend.test;

import com.neuromove.backend.global.api.ApiResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
public class TestController {

    @GetMapping("/api/test")
    public ApiResponse<Map<String, String>> test() {
        return ApiResponse.success("테스트 성공", Map.of("name", "neuromove"));
    }

    @GetMapping("/api/test/error")
    public ApiResponse<Void> errorTest() {
        throw new IllegalArgumentException("잘못된 요청입니다.");
    }
}