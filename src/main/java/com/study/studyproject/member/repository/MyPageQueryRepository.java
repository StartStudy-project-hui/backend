package com.study.studyproject.member.repository;

import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.Predicate;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.JPAExpressions;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.study.studyproject.board.domain.ConnectionType;
import com.study.studyproject.main.dto.ListResponseDto;
import com.study.studyproject.board.domain.Board;
import com.study.studyproject.board.domain.Category;
import com.study.studyproject.board.domain.Recruit;
import com.study.studyproject.main.dto.QListResponseDto;
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
import static com.study.studyproject.member.domain.QMember.member;
import static com.study.studyproject.reply.domain.QReply.reply;
import static org.springframework.util.StringUtils.isEmpty;

@Repository
public class MyPageQueryRepository {

    private final JPAQueryFactory queryFactory;

    public MyPageQueryRepository(EntityManager em) {
        this.queryFactory = new JPAQueryFactory(em);
    }

    public Page<ListResponseDto> MyPageListPage(MemberListRequestDto condition, Pageable pageable, Long memeberId, String getRole) {

        List<ListResponseDto> content = getContent(memeberId,condition, pageable,getRole);

        JPAQuery<Board> countQuery = getTotal(memeberId,condition,getRole);

        return PageableExecutionUtils.getPage(content,pageable, countQuery::fetchCount);
    }

    public List<ListResponseDto> getContent(Long memeberId, MemberListRequestDto condition, Pageable pageable, String getRole) {

        return queryFactory
                .select(
                        new QListResponseDto(
                                member.nickname,
                                board.id.intValue(),
                                board.recruit.stringValue(),
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
                .from(board)
                .join(board.member, member)
                .where(
                        getType(condition.getRecruit()), //모집여부
                        getUser(memeberId), //사용자 아이디 유무
                        getCategory(condition.getCategory()),
                        getConnectionType(condition.getConnectionType()),
                        getAdminPage(getRole)

                )
                .orderBy(
                        getOrder(condition.getOrder()) // 순서
                )
                .groupBy(board)
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();
    }

    private Predicate getAdminPage(String getRole) {
        return getRole.equals("admin") ? board.isDeleted.eq(false) : null;
    }

    //cs
    private BooleanExpression getCategory(Category category) {
        if(isEmpty(category)){
            return null;
        }

        return category.equals(Category.전체) ? null : board.category.eq(category);
    }

    public JPAQuery<Board> getTotal(Long memeberId, MemberListRequestDto condition, String getRole) {
        return queryFactory
                .select(
                        board
                )
                .from(board)
                .where(
                        getType(condition.getRecruit()), //모집여부
                        getUser(memeberId), //사용자 이메일
                        getCategory(condition.getCategory()),
                        getConnectionType(condition.getConnectionType()),
                        getAdminPage(getRole)

                );
    }

    private Predicate getConnectionType(ConnectionType connectionType) {
        System.out.println("connectionType = " + connectionType);
        if (isEmpty(connectionType)) {
            return null;
        }
        return  connectionType.equals(ONLINE) ? board.connectionType.eq(ONLINE) :board.connectionType.eq(OFFLINE);
    }

    private BooleanExpression getType(Recruit type) {


        return isEmpty(type) ? null : board.recruit.eq(type);
    }



    private BooleanExpression getUser(Long userId) {

        return isEmpty(userId) ? null : board.member.id.eq(userId);
    }

    private OrderSpecifier<?> getOrder(int num) {
        if (num == 0) {
            return board.createdDate.desc();
        } else {
             return board.viewCount.desc();
        }
    }


}
