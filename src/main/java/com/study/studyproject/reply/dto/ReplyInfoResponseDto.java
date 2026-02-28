package com.study.studyproject.reply.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.study.studyproject.reply.domain.Reply;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Getter
@ToString
public class ReplyInfoResponseDto {
    private Long replyId;
    private Long parentId;
    private String nickname;
    private String content;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss", timezone = "Asia/Seoul")
    private LocalDateTime updateTime;
    private List<ReplyInfoResponseDto> children = new ArrayList<>();

    public ReplyInfoResponseDto(Reply reply) {
        this.replyId = reply.getId();
        this.updateTime = reply.getLastModifiedDate();
        this.nickname = reply.getNickname();
        this.parentId = (reply.getParent() != null) ? reply.getParent().getId() : null;
        this.content = reply.getContent();
    }

    public static ReplyInfoResponseDto from(Reply reply) {
        return new ReplyInfoResponseDto(reply);
    }


}
