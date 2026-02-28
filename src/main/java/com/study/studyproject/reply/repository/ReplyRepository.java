package com.study.studyproject.reply.repository;

import com.study.studyproject.reply.domain.Reply;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ReplyRepository extends JpaRepository<Reply,Long>, ReplyRepositoryCustom{

    // ReplyRepository 예시
    @Query("select r from Reply r " +
            "left join fetch r.parent " +   // 부모 댓글 페치 조인
            "join fetch r.member " +        // 작성자 정보도 필요하다면 페치 조인
            "where r.board.id = :boardId " +
            "order by r.id asc")
    List<Reply> findByBoardReply(@Param("boardId") Long boardId);

}
