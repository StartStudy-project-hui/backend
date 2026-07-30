package com.study.studyproject.reply.repository;


import com.querydsl.jpa.impl.JPAQueryFactory;
import com.study.studyproject.reply.domain.Reply;
import jakarta.persistence.EntityManager;

import java.util.List;
import java.util.Optional;

import static com.study.studyproject.board.domain.QBoard.board;
import static com.study.studyproject.reply.domain.QReply.reply;


public class ReplyRepositoryImpl implements ReplyRepositoryCustom{

    private final JPAQueryFactory queryFactory;
    public ReplyRepositoryImpl(EntityManager em) {
        this.queryFactory = new JPAQueryFactory(em);
    }


    @Override
    public List<Reply> findByBoardReply(Long boardId) {

        return queryFactory.selectFrom(reply)
                .where(reply.board.id.eq(boardId))
                .orderBy(reply.parent.id.asc().nullsFirst(),
                        reply.createdDate.asc())
                .fetch();
    }

    @Override
    public List<Reply> findByBoardReplies(Long boardId) {
        return queryFactory.selectFrom(reply)
                .where(reply.board.id.eq(boardId))
                .fetch();


    }


    @Override
    public Optional<Reply> findCommentByIdWithParent(Long id) {

        Reply selectedComment = queryFactory.select(reply)
                .from(reply)
                .leftJoin(reply.parent).fetchJoin()
                .where(reply.id.eq(id))
                .fetchOne();

        return Optional.ofNullable(selectedComment);
    }

    @Override
    public Long findBoardReplyCnt(Long id) { // 게시글 넘
        return  queryFactory.select(reply)
                .from(reply)
                .where(
                        board.id.eq(id)
                        , reply.isDeleted.eq(false)
                ).stream().count();
    }


}
