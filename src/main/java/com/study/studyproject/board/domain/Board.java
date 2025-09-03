package com.study.studyproject.board.domain;

import com.study.studyproject.board.dto.BoardReUpdateRequestDto;
import com.study.studyproject.global.config.BaseTimeEntity;
import com.study.studyproject.global.exception.ex.NotFoundException;
import com.study.studyproject.member.domain.Member;
import com.study.studyproject.postlike.domain.PostLike;
import com.study.studyproject.reply.domain.Reply;
import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;
import org.hibernate.annotations.ColumnDefault;

import java.util.List;
import java.util.Objects;

import static com.study.studyproject.board.domain.Recruit.*;
import static com.study.studyproject.global.exception.ex.ErrorCode.UNABLE_DELETE_BOARD;
import static com.study.studyproject.postlike.domain.PostLike.isPostLikes;
import static com.study.studyproject.reply.domain.Reply.isReplies;

@Getter
@Entity
@NoArgsConstructor
@ToString(of = {"id", "title", "viewCount","content","category","recruit"})
public class Board extends BaseTimeEntity {

    @Id @GeneratedValue
    @Column(name = "board_id")
    private Long id;

    @ManyToOne
    @JoinColumn(name = "member_id")
    private Member member;

    private String title;


    @ColumnDefault("0")
    private Long viewCount;

    private String content;


    @Enumerated(EnumType.STRING)
    private ConnectionType connectionType;

    @Embedded
    private OfflineLocation offlineLocation;

    @Enumerated(EnumType.STRING)
    private Category category;

    @Enumerated(EnumType.STRING)
    private Recruit recruit;

    @ColumnDefault("FALSE")
    @Column(nullable = false)
    private Boolean isDeleted;


    @OneToMany(mappedBy = "board") //지연로딩
    private List<PostLike> postLikes;


    @OneToMany(mappedBy = "board") //지연로딩
    private List<Reply> replies;

    public void ChangeBoardIsDeleted(Boolean deleted) {
        isDeleted = deleted;
    }

    public void deleteBoardContent(String title,String content) {
        this.title = title;
        this.content = content;

    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Board board = (Board) o;
        return Objects.equals(id, board.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Builder
    public Board(Member member, String title, String content, Category category, ConnectionType connectionType, OfflineLocation offlineLocation) {
        this.member = member;
        this.title = title;
        this.viewCount = 0L;
        this.content = content;
        this.category = category;
        this.connectionType = connectionType;
        this.offlineLocation = offlineLocation;
        this.recruit = 모집중;
        this.isDeleted = false;

    }

    public Board updateBoard(BoardReUpdateRequestDto boardReUpdateRequestDto){
        this.title = boardReUpdateRequestDto.getTitle();
        this.content = boardReUpdateRequestDto.getContent();
        this.category = boardReUpdateRequestDto.getCategory();
        this.offlineLocation = boardReUpdateRequestDto.getOfflineLocation();
        this.connectionType = boardReUpdateRequestDto.getConnectionType();
        return this;
    }

    public void changeRecuritBoard(){
        this.recruit = this.getRecruit().equals(모집중) ? 모집완료 : 모집중;
    }


    public Board changeAdminDeleteBoard(){
        this.ChangeBoardIsDeleted(true);
        this.deleteBoardContent("관리자로 의해 게시글 삭제","관리자로 의해 게시글 삭제되었습니다.");
        return this;
    }



    //조회수 증가
    public void updateViewCnt(){
        viewCount++;
    }


    public static void validateDeleteBoard(List<Reply> replies, List<PostLike> postLikes) throws NotFoundException {
        if (isReplies(replies) || isPostLikes(postLikes)) {
            throw new NotFoundException(UNABLE_DELETE_BOARD);
        }
    }






}
