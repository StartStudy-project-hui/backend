package com.study.studyproject.member.dto;

import com.study.studyproject.board.domain.Category;
import com.study.studyproject.board.domain.ConnectionType;
import com.study.studyproject.board.domain.RecruitStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

@Data
@NoArgsConstructor
public class MemberListRequestDto  {

    @Schema(description = "모집구분", defaultValue = "모집중")
    RecruitStatus recruitStatus;   // 모집중

    @Schema(description = "카테고리", defaultValue = "CS")
    Category category;   // cs

    @Schema(description = "순서", defaultValue = "최신순")
    int order ; // 0 -> 최신순

    @Schema(description = "온/오프라인 여부",defaultValue = "OFFLINE")
    ConnectionType connectionType;


    
    @Builder
    public MemberListRequestDto(RecruitStatus recruitStatus, Category category, int order, ConnectionType connectionType) {
        this.recruitStatus = recruitStatus;
        this.category = category;
        this.order = order;
        this.connectionType = connectionType;
    }
}
