package com.study.studyproject.board.dto;

import com.study.studyproject.board.domain.Board;
import com.study.studyproject.board.domain.Category;
import com.study.studyproject.board.domain.ConnectionType;
import com.study.studyproject.board.domain.OfflineLocation;
import com.study.studyproject.member.domain.Member;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;


//Member member, String title, Long viewCount, String content, String nickname, Category category
@Data
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class BoardWriteRequestDto {


    @Schema(description = "변경할 내용", defaultValue = "수정 내용")
    @NotBlank(message = "내용을 입력해주세요")
    String content;

    @Schema(description = "카테고리", defaultValue = "CS")
    @NotNull(message = "카테고리를 입력해주세요")
    Category category;

    @Schema(description = "제목", defaultValue = "제목")
    @NotBlank(message = "제목을 입력해주세요")
    String title;

    @Schema(description = "타입", defaultValue = "OFFLINE")
    ConnectionType connectionType;

    OfflineLocation offlineLocation;





    public Board toEntity(Member member) {
        return Board.builder()
                .member(member)
                .title(title)
                .content(content)
                .category(category)
                .connectionType(connectionType)
                .offlineLocation(offlineLocation)
                .build();
    }

    @Builder
    public BoardWriteRequestDto(String content, Category category, String title) {
        this.content = content;
        this.category = category;
        this.title = title;
    }


}
