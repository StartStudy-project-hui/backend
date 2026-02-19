package com.study.studyproject.blacklist.dto.response;

import com.querydsl.core.annotations.QueryProjection;
import com.study.studyproject.blacklist.domain.BlackList;
import com.study.studyproject.blacklist.domain.BlackType;
import com.study.studyproject.blacklist.domain.BlacklistStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
public class BlacklistResponseDto {
    @Schema(description = "블랙리스트 히스토리 id", example = "1")
    private Long id;

    @Schema(description = "블랙리스트 히스토리 타입")
    private BlackType type;

    @Schema(description = "블랙리스트 히스토리 이유", example= "욕설")
    private String reason;

    @Schema(description = "블랙리스트 히스토리 생성일", defaultValue = "2019-01-21:11:22")
    private LocalDateTime createdAt;

    @Schema(description = "블랙리스트 히스토리 상태", defaultValue = "2019-01-21:11:22")
    private BlacklistStatus status;

    @QueryProjection
    public BlacklistResponseDto(Long id, BlackType type, String reason, LocalDateTime createdAt, BlacklistStatus status) {
        this.id = id;
        this.type = type;
        this.reason = reason;
        this.createdAt = createdAt;
        this.status = status;
    }

    public static BlacklistResponseDto from(BlackList blacklist) {
        return new BlacklistResponseDto(
                blacklist.getId(),
                blacklist.getType(),
                blacklist.getReason(),
                blacklist.getCreatedAt(),
                blacklist.getStatus()
        );
    }
}
