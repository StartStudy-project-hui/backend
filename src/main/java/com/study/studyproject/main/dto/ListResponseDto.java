package com.study.studyproject.main.dto;

import com.querydsl.core.annotations.QueryProjection;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;

@Data
public class ListResponseDto {

    @Schema(description = "닉네임", defaultValue = "admin")
    String nickname;
    @Schema(description = "게시글 Id", defaultValue = "1")
    int boardId;

    @Schema(description = "모집여부", defaultValue = "모집중")
    String recurit; //모집중

    @Schema(description = "카테고리", defaultValue = "코테")
    String type; // cs

    @Schema(description = "온/오프라인인지", defaultValue = "OFFLINE")
    String connectionType;

    @Schema(description = "내용", defaultValue = "코테 모집합니다!!!!!!")
    String content;

    @Schema(description = "제목", defaultValue = "제목입니다.")
    String title;

    @DateTimeFormat(pattern = "yyyy-mm-dd'T'HH:mm:ss")
    LocalDateTime time;

    @Schema(description = "조회수", defaultValue = "1")
    int hitCnt; // 조회수

    @Schema(description = "댓글 수 ", defaultValue = "3")
    Long replyCnt; //
    @QueryProjection
    public ListResponseDto(String nickname, int boardId, String recurit, String type, String connectionType, String content, String title, LocalDateTime time, int hitCnt, Long replyCnt) {
        this.nickname = nickname;
        this.boardId = boardId;
        this.recurit = recurit;
        this.type = type;
        this.connectionType = connectionType;
        this.content = content;
        this.title = title;
        this.time = time;
        this.hitCnt = hitCnt;
        this.replyCnt = replyCnt;
    }
}
