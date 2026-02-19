package com.study.studyproject.blacklist.dto.response;


import com.querydsl.core.annotations.QueryProjection;
import com.study.studyproject.blacklist.domain.BlackListHistory;
import com.study.studyproject.blacklist.domain.BlackType;
import com.study.studyproject.blacklist.domain.BlacklistAction;
import com.study.studyproject.blacklist.domain.BlacklistStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class BlacklistHistoryMemberResponseDto {
    @Schema(description = "블랙리스트 id", defaultValue = "1")
    private Long id;
    @Schema(description = "블랙리스트 히스토리 이유", defaultValue = "욕설")
    private String reason;

    @Schema(description = "블랙리스트 히스토리 생성일", defaultValue = "2019-01-21:11:22")
    private LocalDateTime createdAt;

    @Schema(description = "블랙리스트 히스토리 생성일", defaultValue = "2019-01-21:11:22")
    private BlackType type;

    @Schema(description = "블랙리스트 히스토리 상태", defaultValue = "2019-01-21:11:22")
    private BlacklistStatus status;
    @Schema(description = "블랙리스트 히스토리 ", defaultValue = "2019-01-21:11:22")
    private BlacklistAction action;


    @QueryProjection
    public BlacklistHistoryMemberResponseDto(Long id, String reason, LocalDateTime createdAt, BlackType type, BlacklistStatus status, BlacklistAction action) {
        this.id = id;
        this.reason = reason;
        this.createdAt = createdAt;
        this.type = type;
        this.status = status;
        this.action = action;
    }



    public static BlacklistHistoryMemberResponseDto from(BlackListHistory history) {
        return BlacklistHistoryMemberResponseDto.builder()
                .id(history.getId())
                .reason(history.getReason())
                .action(history.getAction())
                .type(history.getType())
                .createdAt(history.getCreateAt())
                .build();
    }

}
