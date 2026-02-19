package com.study.studyproject.blacklist.dto.request;

import com.study.studyproject.blacklist.domain. BlackType;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class BlackListCreateRequestDto {


    @Schema(description = "이메일/ip", example = "kimkim@gmail.com")
    private String rawValue;

    @Schema(description = "블랙리스트 타입", example = "EMAIL")
    private BlackType type;

    @Schema(description = "블랙리스트 이유", example = "욕설")
    private String reason;
    @Schema(description = "블랙리스트 기간", example = "1")
    private int durationMonths;



}
