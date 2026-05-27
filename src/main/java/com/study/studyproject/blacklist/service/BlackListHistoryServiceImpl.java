package com.study.studyproject.blacklist.service;

import com.study.studyproject.blacklist.dto.request.BlackListHistoryMainRequestDto;
import com.study.studyproject.blacklist.dto.response.BlacklistHistoryAdminResponseDto;
import com.study.studyproject.blacklist.dto.response.BlacklistHistoryMemberResponseDto;
import com.study.studyproject.blacklist.repository.blacklisthistory.BlackListHistoryRepository;
import com.study.studyproject.global.hash.HashUtil;
import com.study.studyproject.global.exception.ex.NotFoundException;
import com.study.studyproject.member.domain.Email;
import com.study.studyproject.member.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import static com.study.studyproject.global.exception.ex.ErrorCode.NOT_FOUND_MEMBER;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class BlackListHistoryServiceImpl implements BlackListHistoryService {

    private final MemberRepository memberRepository;
    private final BlackListHistoryRepository blackListHistoryRepository;

    @Override
    public Page<BlacklistHistoryAdminResponseDto> blackHistoryAdminList(BlackListHistoryMainRequestDto blackListHistoryMainRequestDto, Pageable pageable) {
        return blackListHistoryRepository.blackListHistorySearchPageMainList(blackListHistoryMainRequestDto, pageable);
    }

    @Override
    public Slice<BlacklistHistoryMemberResponseDto> findSliceBlackHistoryById(Long id, Pageable pageable) {
        // 해당 닉네임 가져와서
        Email email = memberRepository.findEmailById(id).orElseThrow(() -> new NotFoundException(NOT_FOUND_MEMBER));
        String hash = HashUtil.sha256(email.address());
        return blackListHistoryRepository.searchSliceByEmail(hash, pageable);
    }



}
