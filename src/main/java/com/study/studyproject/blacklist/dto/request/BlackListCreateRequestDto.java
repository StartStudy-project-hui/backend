package com.study.studyproject.blacklist.dto.request;

import com.study.studyproject.blacklist.domain. BlackType;
import com.study.studyproject.member.domain.Email;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class BlackListCreateRequestDto {


    @Schema(description = "이메일", example = "kimkim@gmail.com")
    private Email rawValue;

    @Schema(description = "블랙리스트 이유", example = "욕설")
    private String reason;
    @Schema(description = "블랙리스트 기간", example = "1")
    private int durationMonths;



}
