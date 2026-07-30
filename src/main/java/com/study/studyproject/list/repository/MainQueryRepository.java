package com.study.studyproject.list.repository;

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
import com.study.studyproject.list.dto.MainRequestDto;
import com.study.studyproject.list.dto.QListResponseDto;
import jakarta.persistence.EntityManager;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.support.PageableExecutionUtils;
import org.springframework.stereotype.Repository;

import java.util.List;

import static com.study.studyproject.board.domain.ConnectionType.OFFLINE;
import static com.study.studyproject.board.domain.ConnectionType.ONLINE;
import static com.study.studyproject.board.domain.QBoard.board;
import static com.study.studyproject.reply.domain.QReply.reply;
import static org.springframework.util.StringUtils.isEmpty;

@Repository
public class MainQueryRepository  {

    private final JPAQueryFactory queryFactory;

    public MainQueryRepository(EntityManager em) {
        this.queryFactory = new JPAQueryFactory(em);
    }

    public Page<ListResponseDto> getBoardListPage(String findContent, MainRequestDto condition, Pageable pageable) {

        List<ListResponseDto> content = getContent(findContent,condition, pageable);
        JPAQuery<Long> countQuery = getTotal(findContent,condition);

        return PageableExecutionUtils.getPage(content,pageable,   () -> {
            Long count = countQuery.fetchOne();
            return count != null ? count : 0L;
        });
    }

    public List<ListResponseDto> getContent(String findContent, MainRequestDto condition, Pageable pageable) {

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
                        )
                )
                .from(board)
                .where(
                        board.recruitStatus.eq(RecruitStatus.RECRUITING),
                        getCategory(condition.getCategory()),
                        getFindContent(findContent),
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

    private Predicate getConnectionType(ConnectionType connectionType) {
        if (isEmpty(connectionType)) {
            return null;
        }
        return  connectionType.equals(ONLINE) ? board.connectionType.eq(ONLINE) :board.connectionType.eq(OFFLINE);
    }

    //cs
    private Predicate getCategory(Category category) {
        if(isEmpty(category)){
            return null;
        }

        return category.equals(Category.ALL) ? null : board.category.eq(category);
    }

    public JPAQuery<Long> getTotal(String findContent, MainRequestDto condition) {
        return queryFactory
                .select(
                        board.count()
                )
                .from(board)
                .where(
                        board.recruitStatus.eq(RecruitStatus.RECRUITING),
                        getCategory(condition.getCategory()),
                        getFindContent(findContent),
                        getConnectionType(condition.getConnectionType()),
                        board.isDeleted.eq(false)
                );
    }



    private BooleanExpression getFindContent(String findContent) {
        return isEmpty(findContent) ? null : board.title.startsWith(findContent);
    }



    private OrderSpecifier<?> getOrder(int num) {
        if (num == 0) {
            return board.createdDate.desc();
        } else {
             return board.viewCount.desc();
        }
    }


}
