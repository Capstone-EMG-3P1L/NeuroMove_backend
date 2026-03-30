package com.neuromove.backend.test;

import com.neuromove.backend.global.api.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.context.annotation.Profile;

import java.util.Map;

@Profile({"local", "dev"})
@Tag(name = "Test API", description = "테스트용 API입니다.")
@RestController
public class TestController {

    @Operation(summary = "정상 응답 테스트", description = "공통 응답 포맷이 정상적으로 동작하는지 확인합니다.")
    @GetMapping("/api/test")
    public ApiResponse<Map<String, String>> test() {
        return ApiResponse.success("테스트 성공", Map.of("name", "neuromove"));
    }

    @Operation(summary = "예외 응답 테스트", description = "공통 예외 처리 응답이 정상적으로 동작하는지 확인합니다.")
    @GetMapping("/api/test/error")
    public ApiResponse<Void> errorTest() {
        throw new IllegalArgumentException("잘못된 요청입니다.");
    }
}