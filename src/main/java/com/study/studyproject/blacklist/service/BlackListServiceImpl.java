package com.study.studyproject.blacklist.service;

import com.study.studyproject.blacklist.domain.*;
import com.study.studyproject.blacklist.dto.request.BlackListCreateRequestDto;
import com.study.studyproject.blacklist.dto.request.BlackListMainRequestDto;
import com.study.studyproject.blacklist.dto.request.BlackListUpdateRequestDto;
import com.study.studyproject.blacklist.dto.response.BlacklistResponseDto;
import com.study.studyproject.blacklist.repository.blacklisthistory.BlackListHistoryRepository;
import com.study.studyproject.blacklist.repository.blacklist.BlackListRepository;
import com.study.studyproject.global.GlobalResultDto;
import com.study.studyproject.global.hash.HashUtil;
import com.study.studyproject.global.exception.ex.NotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import static com.study.studyproject.blacklist.domain.BlacklistAction.*;
import static com.study.studyproject.global.exception.ex.ErrorCode.NOT_FOUND_MEMBER;

@Service
@RequiredArgsConstructor
@Transactional
public class BlackListServiceImpl implements BlackListService {
    private final BlackListRepository blacklistRepository;
    private final BlackListHistoryRepository blackListHistoryRepository;

    @Override
    public GlobalResultDto registerOrUpdateBlackList(BlackListCreateRequestDto request) {

        String hash = HashUtil.sha256(request.getRawValue());
        BlackList findByBlackList = blacklistRepository.findByHashValue(hash)
                .orElseGet(() -> BlackList.create( hash, request.getReason()));

        //history reposeitory에서 가져오기
        long violationCount = blackListHistoryRepository.countByHashValueAndAction(hash, REGISTER);
        BlacklistStatus blacklistStatus = findByBlackList.setDuration(request.getDurationMonths(), violationCount);


        if (findByBlackList.isNew()) { //값이 없다면
            blacklistRepository.save(findByBlackList);
        }

        if(findByBlackList.existsInDB()){
            findByBlackList.updateReason(request.getReason());
        }

        BlackListHistory blackListHistory = BlackListHistory.save(BlacklistAction.REGISTER, findByBlackList,hash,BlackType.EMAIL , request.getReason(), blacklistStatus);
        blackListHistoryRepository.save(blackListHistory);

        return new GlobalResultDto("블랙리스트 등록 완료", HttpStatus.OK.value());
    }

    @Override
    public GlobalResultDto update(Long id, BlackListUpdateRequestDto dto) {
        BlackList blacklist = blacklistRepository.findById(id)
                .orElseThrow(() -> new NotFoundException(NOT_FOUND_MEMBER));

        blacklist.updateReason(dto.getReason());
        BlackListHistory save = BlackListHistory.save(UPDATE,blacklist,blacklist.getHashValue(),BlackType.EMAIL,  blacklist.getReason(), blacklist.getStatus());
        blackListHistoryRepository.save(save);

        return new GlobalResultDto("블랙리스트 수정 완료", HttpStatus.OK.value());

    }

    @Override
    public GlobalResultDto delete(Long id) {

        BlackList blacklist = blacklistRepository.findById(id)
                .orElseThrow(() -> new NotFoundException(NOT_FOUND_MEMBER));

        // 히스토리엔 저
        BlackListHistory save = BlackListHistory.save(BlacklistAction.DELETE,blacklist,blacklist.getHashValue(),  BlackType.EMAIL,blacklist.getReason(), BlacklistStatus.EXPIRED);
        blackListHistoryRepository.save(save);
        blacklistRepository.delete(blacklist);

        return new GlobalResultDto("블랙리스트 삭제 완료", HttpStatus.OK.value());
    }

    @Override
    public GlobalResultDto makePermanent(Long id) {
        BlackList blacklist = blacklistRepository.findById(id)
                .orElseThrow(() -> new NotFoundException(NOT_FOUND_MEMBER));

        blacklist.makePermanent();
        BlackListHistory save = BlackListHistory.save(BlacklistAction.EXTEND, blacklist, blacklist.getHashValue(), BlackType.EMAIL, blacklist.getReason(), blacklist.getStatus());
        blackListHistoryRepository.save(save);

        return new GlobalResultDto("영구정지로 변경되었습니다", HttpStatus.OK.value());
    }

    // 페이징 처리
    @Override
    @Transactional(readOnly = true)
    public Page<BlacklistResponseDto> findPageBlackList(BlackListMainRequestDto blackListMainRequestDto, Pageable pageable) {

        String hashedEmail = StringUtils.hasText(blackListMainRequestDto.getEmail())
                ? HashUtil.sha256(blackListMainRequestDto.getEmail().trim())
                : null;

        return blacklistRepository.blackListSearchPageMainList(blackListMainRequestDto, hashedEmail,pageable);

    }



}
