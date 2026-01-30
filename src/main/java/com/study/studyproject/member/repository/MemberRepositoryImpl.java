package com.study.studyproject.member.repository;

import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.study.studyproject.member.domain.Member;
import com.study.studyproject.member.dto.UserInfoResponseDto;
import jakarta.persistence.EntityManager;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.support.PageableExecutionUtils;

import java.util.List;

import static com.study.studyproject.member.domain.QMember.member;
import static org.springframework.util.StringUtils.isEmpty;

public class MemberRepositoryImpl implements MemberRepositoryCustom{
    private final JPAQueryFactory queryFactory;

    public MemberRepositoryImpl(EntityManager em) {
        this.queryFactory = new JPAQueryFactory(em);
    }


    @Override
    public Page<UserInfoResponseDto> adminUserBoardList(String username,List<UserInfoResponseDto> userInfoResponseDtos , Pageable pageable) {

        JPAQuery<Member> countQuery = getTotal(username);
        return PageableExecutionUtils.getPage(userInfoResponseDtos,pageable, countQuery::fetchCount);

    }

    @Override
    public List<Member> getContent(String username, Pageable pageable) {

        return queryFactory.
                selectFrom(member)
                .where(getUsername(username))
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();
    }

    private BooleanExpression getUsername(String username) {
        return isEmpty(username) ? null : member.username.startsWith(username);
    }

    public JPAQuery<Member> getTotal(String username) {
        return queryFactory.
                selectFrom(member)
                .where(getUsername(username))

        ;
    }
}
