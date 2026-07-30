package com.study.studyproject.member.repository;

import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.Predicate;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.JPAExpressions;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.study.studyproject.board.domain.Board;
import com.study.studyproject.board.domain.Category;
import com.study.studyproject.board.domain.ConnectionType;
import com.study.studyproject.board.domain.RecruitStatus;
import com.study.studyproject.list.dto.ListResponseDto;
import com.study.studyproject.list.dto.QListResponseDto;
import com.study.studyproject.member.dto.MemberListRequestDto;
import jakarta.persistence.EntityManager;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.support.PageableExecutionUtils;
import org.springframework.stereotype.Repository;

import java.util.List;

import static com.study.studyproject.board.domain.ConnectionType.OFFLINE;
import static com.study.studyproject.board.domain.ConnectionType.ONLINE;
import static com.study.studyproject.board.domain.QBoard.board;
import static com.study.studyproject.postlike.domain.QPostLike.postLike;
import static com.study.studyproject.reply.domain.QReply.reply;
import static org.springframework.util.StringUtils.isEmpty;

@Repository
public class  MyPagePostLikeQueryRepository{

    private final JPAQueryFactory queryFactory;

    public MyPagePostLikeQueryRepository(EntityManager em) {
        this.queryFactory = new JPAQueryFactory(em);
    }

    public Page<ListResponseDto> MyPageListPage(MemberListRequestDto condition, Pageable pageable, Long memeberId) {

        List<ListResponseDto> content = getContent(memeberId,condition, pageable);

        JPAQuery<Board> countQuery = getTotal(memeberId,condition);

        return PageableExecutionUtils.getPage(content,pageable, countQuery::fetchCount);
    }

    public List<ListResponseDto> getContent(Long memeberId, MemberListRequestDto condition, Pageable pageable) {
        return queryFactory
                .select(
                        new QListResponseDto(
                                board.member.nickname,
                                board.id.intValue(),
                                board.recruitStatus.stringValue(),
                                board.category.stringValue(),
                                board.connectionType.stringValue(),
                                board.content,
                                board.title,
                                board.createdDate,
                                board.viewCount.intValue(),
                                JPAExpressions
                                        .select(reply.count())
                                        .from(reply)
                                        .where(reply.isDeleted.eq(false)
                                                .and(reply.board.id.eq(board.id)))
                        ))
                .from(postLike)
                .innerJoin(board).fetchJoin()
                .on(postLike.board.id.eq(board.id))
                .where(
                        getType(condition.getRecruitStatus()), //모집여부
                        getPostLikeMember(memeberId), //사용자 아이디 유무
                        getCategory(condition.getCategory()),
                        getConnectionType(condition.getConnectionType()),
                        board.isDeleted.eq(false)
                )
                .orderBy(
                        getOrder(condition.getOrder()) // 순서
                )
                .groupBy(board)
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();
    }


    //cs
    private BooleanExpression getCategory(Category category) {
        if(isEmpty(category)){
            return null;
        }

        return category.equals(Category.ALL) ? null : board.category.eq(category);
    }

    public JPAQuery<Board> getTotal(Long memeberId, MemberListRequestDto condition) {
        return queryFactory
                .select(
                        board
                )
                .from(board)
                .where(
                        getType(condition.getRecruitStatus()), //모집여부
                        getPostLikeMember(memeberId), //사용자 이메일
                        getCategory(condition.getCategory()),
                        getConnectionType(condition.getConnectionType()),
                        board.isDeleted.eq(false)
                );
    }

    private Predicate getConnectionType(ConnectionType connectionType) {
        if (connectionType == null) {
            return null;
        }
        return  connectionType.equals(ONLINE) ? board.connectionType.eq(ONLINE) :board.connectionType.eq(OFFLINE);
    }

    private BooleanExpression getType(RecruitStatus type) {

        return type == null ? null : board.recruitStatus.eq(type);
    }



    private BooleanExpression getPostLikeMember(Long userId) {
        return userId == null ? null : postLike.member.id.eq(userId);
    }

    private OrderSpecifier<?> getOrder(int num) {
        if (num == 0) {
            return board.createdDate.desc();
        } else {
            return board.viewCount.desc();
        }
    }


}
