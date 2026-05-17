package com.study.studyproject.board.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.study.studyproject.board.domain.*;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class BoardOneResponseDto {

    Recruit recruit;
    @Schema(description = "현재 닉네임", defaultValue = "jacom!!")
    @JsonInclude(JsonInclude.Include.NON_DEFAULT)
    String currentNickname;

    @Schema(description = "제목", defaultValue = "제목제목")
    String title;

    @Schema(description = "게시글 작성자", defaultValue = "jacom!!!")
    String boardWriteNickname;


    @Schema(description = "수정 시간", defaultValue = "2023-10-05T12:34:56")
    LocalDateTime updateTime;

    @Schema(description = "작성 시간", defaultValue = "2023-10-05T12:34:56")
    LocalDateTime createTime;

    @Schema(description = "게시글 내용", defaultValue = "내요내용")
    String content;

    Category category;
    
    @Schema(description = "조회수", defaultValue = "3")
    int viewCnt;

    @Schema(description = "타입", defaultValue = "오프라인")
    ConnectionType connectionType;

    @JsonInclude(JsonInclude.Include.NON_DEFAULT)
    OfflineLocation offlineLocation;

    @Builder
    public BoardOneResponseDto(Recruit recruit, String currentNickname, String title, String boardWriteNickname, LocalDateTime updateTime, LocalDateTime createTime, String content, Category category, int viewCnt,ConnectionType type, OfflineLocation offlineLocation) {
        this.recruit = recruit;
        this.currentNickname = currentNickname;
        this.title = title;
        this.boardWriteNickname = boardWriteNickname;
        this.updateTime = updateTime;
        this.createTime = createTime;
        this.content = content;
        this.category = category;
        this.viewCnt = viewCnt;
        this.connectionType = type;
        this.offlineLocation = offlineLocation;
    }



    public static BoardOneResponseDto of(Board board) {

        return BoardOneResponseDto.builder()
                .updateTime(board.getLastModifiedDate())
                .createTime(board.getCreatedDate())
                .title(board.getTitle())
                .recruit(board.getRecruit())
                .content(board.getContent())
                .viewCnt(Math.toIntExact(board.getViewCount()))
                .boardWriteNickname(board.getMember().getNickname())
                .type(board.getConnectionType())
                .offlineLocation(board.getOfflineLocation())
                .category(board.getCategory())
                .build();
    }
    
}

