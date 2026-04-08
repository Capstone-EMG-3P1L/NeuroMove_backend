package com.neuromove.backend.domain.session.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class SessionEndRequest {

    @NotBlank(message = "종료 이유는 필수입니다.")
    @Schema(description = "종료 이유 (USER_REQUEST, SYSTEM_STOP, ERROR, EMERGENCY)", example = "USER_REQUEST")
    private String reason;
}
