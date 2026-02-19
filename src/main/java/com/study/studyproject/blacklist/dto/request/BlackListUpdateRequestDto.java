package com.study.studyproject.blacklist.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Builder;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class BlackListUpdateRequestDto {
    @NotBlank(message = "변경 이유를 작성해주세요 ")
    @Schema(description = "블랙리스트 변경 이유", example = "피싱")
    private String reason;

    @Builder
    public BlackListUpdateRequestDto(String reason) {
        this.reason = reason;
    }
}
