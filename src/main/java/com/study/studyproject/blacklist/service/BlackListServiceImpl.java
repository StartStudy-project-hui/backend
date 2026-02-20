package com.study.studyproject.blacklist.service;

import com.study.studyproject.blacklist.domain.BlackList;
import com.study.studyproject.blacklist.domain.BlackListHistory;
import com.study.studyproject.blacklist.domain.BlacklistAction;
import com.study.studyproject.blacklist.domain.BlacklistStatus;
import com.study.studyproject.blacklist.dto.request.BlackListCreateRequestDto;
import com.study.studyproject.blacklist.dto.request.BlackListMainRequestDto;
import com.study.studyproject.blacklist.dto.request.BlackListUpdateRequestDto;
import com.study.studyproject.blacklist.dto.response.BlacklistResponseDto;
import com.study.studyproject.blacklist.repository.blacklisthistory.BlackListHistoryRepository;
import com.study.studyproject.blacklist.repository.blacklist.BlackListRepository;
import com.study.studyproject.global.GlobalResultDto;
import com.study.studyproject.global.Hash.HashUtil;
import com.study.studyproject.global.exception.ex.NotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
                .orElseGet(() -> BlackList.create(request.getType(), hash, request.getReason()));

        //history reposeitory에서 가져오기
        long violationCount = blackListHistoryRepository.countByHashValueAndAction(hash, REGISTER);
        BlacklistStatus blacklistStatus = findByBlackList.setDuration(request.getDurationMonths(), violationCount);


        if (findByBlackList.isNew()) { //값이 없다면
            blacklistRepository.save(findByBlackList);
        }

        if(findByBlackList.existsInDB()){
            findByBlackList.updateReason(request.getReason());
        }

        BlackListHistory blackListHistory = BlackListHistory.save(BlacklistAction.REGISTER, findByBlackList,hash, request.getType(), request.getReason(), blacklistStatus);
        blackListHistoryRepository.save(blackListHistory);

        return new GlobalResultDto("블랙리스트 등록 완료", HttpStatus.OK.value());
    }

    @Override
    public GlobalResultDto update(Long id, BlackListUpdateRequestDto dto) {
        BlackList blacklist = blacklistRepository.findById(id)
                .orElseThrow(() -> new NotFoundException(NOT_FOUND_MEMBER));

        blacklist.updateReason(dto.getReason());
        BlackListHistory save = BlackListHistory.save(UPDATE,blacklist,blacklist.getHashValue(), blacklist.getType(), blacklist.getReason(), blacklist.getStatus());
        blackListHistoryRepository.save(save);

        return new GlobalResultDto("블랙리스트 수정 완료", HttpStatus.OK.value());

    }

    @Override
    public GlobalResultDto delete(Long id) {

        BlackList blacklist = blacklistRepository.findById(id)
                .orElseThrow(() -> new NotFoundException(NOT_FOUND_MEMBER));

        // 히스토리엔 저
        BlackListHistory save = BlackListHistory.save(BlacklistAction.DELETE,blacklist,blacklist.getHashValue(), blacklist.getType(), blacklist.getReason(), BlacklistStatus.EXPIRED);
        blackListHistoryRepository.save(save);
        blacklistRepository.delete(blacklist);

        return new GlobalResultDto("블랙리스트 삭제 완료", HttpStatus.OK.value());
    }

    // 페이징 처리
    @Override
    @Transactional(readOnly = true)
    public Page<BlacklistResponseDto> findPageBlackList(BlackListMainRequestDto blackListMainRequestDto, Pageable pageable) {
        return blacklistRepository.blackListSearchPageMainList(blackListMainRequestDto, pageable);

    }



}
