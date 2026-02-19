package com.study.studyproject.blacklist.dto.request;

import com.study.studyproject.blacklist.domain.BlackType;
import com.study.studyproject.blacklist.domain.BlacklistAction;
import com.study.studyproject.blacklist.domain.BlacklistStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Setter
public class BlackListHistoryMainRequestDto {

    @Schema(description = "블랙리스트 히스토리 아이디", example = "1")
    private Long id;
    @Schema(description = "블랙리스트 히스토리 타입", example = "EMAIL")
    private BlackType type;

    @Schema(description = "블랙리스트  상태", example = "ACTIVE")
    private BlacklistStatus status;

    @Schema(description = "블랙리스트  액션", example = "REGISTER")
    private BlacklistAction action;


}
