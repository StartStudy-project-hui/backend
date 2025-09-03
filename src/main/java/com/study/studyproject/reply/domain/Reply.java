package com.study.studyproject.reply.domain;

import com.study.studyproject.board.domain.Board;
import com.study.studyproject.global.config.BaseTimeEntity;
import com.study.studyproject.member.domain.Member;
import com.study.studyproject.reply.dto.ReplyRequestDto;
import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;
import org.hibernate.annotations.ColumnDefault;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Entity
@NoArgsConstructor
@Getter
@ToString(of = {"id","content","isDeleted"})
public class Reply extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "reply_id")
    private Long id;

    private String content;
    private String nickname;


    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_id")
    private Reply parent;


    @ColumnDefault("FALSE")
    @Column(nullable = false)
    private Boolean isDeleted;

    @OneToMany(mappedBy = "parent", orphanRemoval = true)
    private List<Reply> children = new ArrayList<>();



    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id")
    private Member member;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "board_id")
    private Board board;

    @Builder
    public Reply(String content, Reply parent, Member member, Board board) {
        this.content = content;
        this.parent = parent;
        this.isDeleted = false;
        this.member = member;
        this.nickname = member.getNickname();
        this.board = board;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Reply reply = (Reply) o;
        return Objects.equals(id, reply.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    public static Reply toEntity(ReplyRequestDto replyRequestDto, Board board, Member member) {
        return Reply.builder()
                .content(replyRequestDto.getContent())
                .member(member)
                .board(board)
                .build();
    }

    public void updateParent(Reply parent) {
        this.parent = parent;
         parent.getChildren().add(this);
    }


    //수정
    public void updateReply(String content) {
        this.content = content;
    }

    public void updateWriter(Member member) {
        this.member = member;
        member.getReplies().add(this);
    }

    public void UpdateBoard(Board board) {
        this.board = board;
        if (board.getReplies() != null) {
            board.getReplies().add(this);
        }
    }

    public void ChangeIsDeleted(Boolean deleted) {
        isDeleted = deleted;
    }

    public static boolean isReplies(List<Reply> replies) {
        return replies.size() != 0;
    }


    public boolean hasChildrenReplies() {
        return this.getChildren().size() != 0;
    }




}
