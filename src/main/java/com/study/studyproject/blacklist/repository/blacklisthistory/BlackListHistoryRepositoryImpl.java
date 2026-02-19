package com.study.studyproject.blacklist.repository.blacklisthistory;

import com.querydsl.core.types.Predicate;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.study.studyproject.blacklist.domain.*;
import com.study.studyproject.blacklist.dto.request.BlackListHistoryMainRequestDto;
import com.study.studyproject.blacklist.dto.response.BlacklistHistoryAdminResponseDto;
import com.study.studyproject.blacklist.domain.BlackListHistory;
import com.study.studyproject.blacklist.dto.response.BlacklistHistoryMemberResponseDto;
import com.study.studyproject.blacklist.dto.response.QBlacklistHistoryAdminResponseDto;
import com.study.studyproject.blacklist.dto.response.QBlacklistHistoryMemberResponseDto;
import jakarta.persistence.EntityManager;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.SliceImpl;
import org.springframework.data.support.PageableExecutionUtils;

import java.util.List;

import static com.study.studyproject.blacklist.domain.QBlackListHistory.*;
import static org.apache.commons.lang3.ObjectUtils.isEmpty;

@Slf4j
public class BlackListHistoryRepositoryImpl implements BlackListHistoryRepositoryCustom {

    private final JPAQueryFactory queryFactory;

    public BlackListHistoryRepositoryImpl(EntityManager em) {
        this.queryFactory = new JPAQueryFactory(em);
    }

    @Override
    public Page<BlacklistHistoryAdminResponseDto> blackListHistorySearchPageMainList(BlackListHistoryMainRequestDto condition, Pageable pageable) {
        List<BlacklistHistoryAdminResponseDto> content = getContent(condition, pageable);
        JPAQuery<BlackListHistory> countQuery = getTotal(condition);

        return PageableExecutionUtils.getPage(content,pageable, () -> countQuery.select(blackListHistory.count()).fetchOne());
    }



    private List<BlacklistHistoryAdminResponseDto> getContent(BlackListHistoryMainRequestDto condition, Pageable pageable) {
        return queryFactory
                .select(new QBlacklistHistoryAdminResponseDto(
                        blackListHistory.id
                        ,blackListHistory.reason, blackListHistory.createAt,blackListHistory.type
                        ,blackListHistory.createBy, blackListHistory.status,
                        blackListHistory.action,blackListHistory.hashValue
                        )
                ).from(blackListHistory)
                .where(
                        idEq(condition.getId()),
                        typeEq(condition.getType()),
                        statusEq(condition.getStatus()),
                        actionEq(condition.getAction())

                ).orderBy(blackListHistory.createAt.desc())
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize()).fetch();

    }

    private JPAQuery<BlackListHistory> getTotal(BlackListHistoryMainRequestDto condition) {
        return queryFactory
                .selectFrom(blackListHistory)
                .where(
                        idEq(condition.getId()),
                        typeEq(condition.getType()),
                        statusEq(condition.getStatus()),
                        actionEq(condition.getAction())

                );
    }

    private BooleanExpression idEq(Long id) {
        return isEmpty(id) ? null : blackListHistory.id.eq(id);
    }

    private BooleanExpression statusEq(BlacklistStatus status) {
        return status == null ? null : blackListHistory.status.eq(status);
    }


    private BooleanExpression typeEq(BlackType type) {
        return type == null ? null : blackListHistory.type.eq(type);
    }

    private Predicate actionEq(BlacklistAction action) {
        return action == null ? null : blackListHistory.action.eq(action);

    }
//

    @Override
    public Slice<BlacklistHistoryMemberResponseDto> searchSliceByEmail(String hash, Pageable pageable) {
        List<BlacklistHistoryMemberResponseDto> result = queryFactory
                .select(new QBlacklistHistoryMemberResponseDto(
                        blackListHistory.id
                        , blackListHistory.reason, blackListHistory.createAt, blackListHistory.type
                        , blackListHistory.status,
                        blackListHistory.action))
                .from(blackListHistory)
                .where(blackListHistory.hashValue.eq(hash))
                .orderBy(blackListHistory.createAt.desc()
                ,    blackListHistory.id.desc()
                )
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize() + 1).fetch();
        return checkNext(result, pageable);
    }

    private Slice<BlacklistHistoryMemberResponseDto> checkNext(List<BlacklistHistoryMemberResponseDto> result, Pageable pageable) {
        boolean hasNext = false;

        if (result.size() > pageable.getPageSize()) {
            hasNext = true;
            result.remove(pageable.getPageSize());
        }
        return new SliceImpl<>(result, pageable, hasNext);

    }



}
