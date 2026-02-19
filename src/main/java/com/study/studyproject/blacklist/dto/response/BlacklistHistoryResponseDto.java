package com.study.studyproject.blacklist.dto.response;


import com.querydsl.core.annotations.QueryProjection;
import com.study.studyproject.blacklist.domain.BlackType;
import com.study.studyproject.blacklist.domain.BlacklistAction;
import com.study.studyproject.blacklist.domain.BlacklistStatus;
import com.study.studyproject.blacklist.domain.BlackListHistory;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class BlacklistHistoryResponseDto {
    @Schema(description = "블랙리스트 id", defaultValue = "1")
    private Long id;
    @Schema(description = "블랙리스트 히스토리 이유", defaultValue = "욕설")
    private String reason;

    @Schema(description = "블랙리스트 히스토리 생성일", defaultValue = "2019-01-21:11:22")
    private LocalDateTime createdAt;

    @Schema(description = "블랙리스트 히스토리 생성일", defaultValue = "2019-01-21:11:22")
    private BlackType type;

    @Schema(description = "블랙리스트 히스토리 등록자", defaultValue = "김하이")
    private String createBy;

    @Schema(description = "블랙리스트 히스토리 상태", defaultValue = "2019-01-21:11:22")
    private BlacklistStatus status;
    @Schema(description = "블랙리스트 히스토리 ", defaultValue = "2019-01-21:11:22")
    private BlacklistAction action;

    @Schema(description = "블랙리스트 히스토리 해시값", defaultValue = "enfaljdkjafdoafsu")
    private String hashValue;

    @QueryProjection
    public BlacklistHistoryResponseDto(Long id, String reason, LocalDateTime createdAt, BlackType type, String createBy, BlacklistStatus status, BlacklistAction action, String hashValue) {
        this.id = id;
        this.reason = reason;
        this.createdAt = createdAt;
        this.type = type;
        this.createBy = createBy;
        this.status = status;
        this.action = action;
        this.hashValue = hashValue;
    }



    public static BlacklistHistoryResponseDto from(BlackListHistory history) {
        return BlacklistHistoryResponseDto.builder()
                .id(history.getId())
                .reason(history.getReason())
                .action(history.getAction())
                .createBy(history.getCreateBy())
                .hashValue(history.getHashValue())
                .type(history.getType())
                .createdAt(history.getCreateAt())
                .build();
    }

}
