package com.study.studyproject.blacklist.repository.blacklist;

import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.study.studyproject.blacklist.domain.BlackList;
import com.study.studyproject.blacklist.domain.BlackType;
import com.study.studyproject.blacklist.domain.BlacklistStatus;
import com.study.studyproject.blacklist.dto.request.BlackListMainRequestDto;
import com.study.studyproject.blacklist.dto.response.BlacklistResponseDto;
import com.study.studyproject.blacklist.dto.response.QBlacklistResponseDto;
import jakarta.persistence.EntityManager;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.support.PageableExecutionUtils;

import java.util.List;

import static com.study.studyproject.blacklist.domain.QBlackList.blackList;
import static org.apache.commons.lang3.ObjectUtils.isEmpty;

public class BlackListRepositoryImpl implements BlackListRepositoryCustom {
    private final JPAQueryFactory queryFactory;
    public BlackListRepositoryImpl(EntityManager em) {
        this.queryFactory = new JPAQueryFactory(em);
    }


    @Override
    public Page<BlacklistResponseDto> blackListSearchPageMainList(BlackListMainRequestDto condition, Pageable pageable) {
        List<BlacklistResponseDto> content = getContent(condition, pageable);
        JPAQuery<BlackList> countQuery = getTotal(condition);

        return PageableExecutionUtils.getPage(content,pageable, () -> countQuery.select(blackList.count()).fetchOne());
    }


    private List<BlacklistResponseDto> getContent(BlackListMainRequestDto condition, Pageable pageable) {

        return queryFactory.
                select(new QBlacklistResponseDto(blackList.id,blackList.type
                ,blackList.reason, blackList.createdAt, blackList.status))
                .from(blackList)
                .where(
                        idEq(condition.getId()),
                        typeEq(condition.getType()),
                        statusEq(condition.getStatus())
                )
                .orderBy(blackList.createdAt.desc())
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize()).fetch();
    }


    private JPAQuery<BlackList> getTotal(BlackListMainRequestDto condition) {
        return queryFactory
                .selectFrom(blackList)
                .where(
                        idEq(condition.getId()),
                        typeEq(condition.getType()),
                        statusEq(condition.getStatus())

                );
    }

    private BooleanExpression idEq(Long id) {
        return isEmpty(id) ? null : blackList.id.eq(id);
    }

    private BooleanExpression statusEq(BlacklistStatus status) {
        return status == null ? null : blackList.status.eq(status);
    }


    private BooleanExpression typeEq(BlackType type) {
        return type == null ? null : blackList.type.eq(type);
    }

}
