package com.study.studyproject.reply.domain;

import com.study.studyproject.board.domain.Board;
import com.study.studyproject.global.config.BaseTimeEntity;
import com.study.studyproject.member.domain.Member;
import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;
import org.hibernate.annotations.ColumnDefault;
import org.hibernate.annotations.Where;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import static jakarta.persistence.FetchType.*;

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


    @ManyToOne(fetch = LAZY)
    @JoinColumn(name = "parent_id")
    private Reply parent;


    @ColumnDefault("FALSE")
    @Column(nullable = false)
    private Boolean isDeleted;

    @OneToMany(mappedBy = "parent", orphanRemoval = true)
    private List<Reply> children = new ArrayList<>();



    @ManyToOne(fetch = LAZY)
    @JoinColumn(name = "member_id")
    private Member member;

    @ManyToOne(fetch = LAZY)
    @JoinColumn(name = "board_id")
    private Board board;

    @Builder
    public Reply(String content, String nickname) {
        this.content = content;
        this.isDeleted = false;
        this.nickname = nickname;
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

    public static Reply createReply(String content, Board board, Member member, Reply parent) {
        Reply reply = Reply.builder()
                .content(content)
                .nickname(member.getNickname())
                .build();

        // 연관관계 편의 메서드들 호출
        reply.assignBoard(board);
        reply.assignWriter(member);

        if (parent != null) {
            reply.assignParent(parent);
        }

        return reply;
    }



    public void anonymize() {
        this.content = "삭제된 댓글입니다.";
        this.nickname = "알수없음";
        this.isDeleted = true;
    }

    public void assignBoard(Board board) {
        this.board = board;
        this.board.getReplies().add(this);
    }

    public void assignWriter(Member member) {
        this.member = member;
        member.getReplies().add(this);
    }


    public void assignParent(Reply parent) {
        this.parent = parent;
        parent.getChildren().add(this);
    }

    //수정
    public void updateReply(String content) {
        this.content = content;
    }


    public void changeIsDeleted(Boolean deleted) {
        isDeleted = deleted;
    }

    public static boolean isReplies(List<Reply> replies) {
        return replies.size() != 0;
    }


    public boolean hasChildrenReplies() {
        return this.getChildren().size() != 0;
    }




}
