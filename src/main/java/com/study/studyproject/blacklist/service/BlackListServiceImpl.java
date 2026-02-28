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
import static com.study.studyproject.global.exception.ex.ErrorCode.MEMBER_NOT_FOUND;

@Service
@RequiredArgsConstructor
@Transactional
public class BlackListServiceImpl implements BlackListService {
    private final BlackListRepository blacklistRepository;
    private final BlackListHistoryRepository blackListHistoryRepository;

    @Override
    public Long registerOrUpdateBlackList(BlackListCreateRequestDto request) {

        String hash = HashUtil.sha256(request.getRawValue());
        BlackList blackList = blacklistRepository.findByHashValueWithLock(hash)
                .map(existing -> {
                    // 기존 데이터가 있으면 업데이트 로직 수행
                    long count = blackListHistoryRepository.countByHashValueAndAction(hash, REGISTER);
                    existing.overwrite(request.getReason(), request.getDurationMonths(), count);
                    return existing;
                })
                .orElseGet(() -> {
                    // 없으면 신규 생성 및 저장
                    return blacklistRepository.save(BlackList.from(request.getDurationMonths(),request.getReason(),request.getType(), hash));
                });

        // 2. 이력 생성 및 저장 (무조건 실행)
        saveHistory(blackList, request, hash);

        return blackList.getId();
    }

    private void saveHistory(BlackList blackList, BlackListCreateRequestDto request, String hash) {
        BlackListHistory history = BlackListHistory.create(
                BlacklistAction.REGISTER,
                blackList,
                hash,
                request.getType(),
                request.getReason(),
                blackList.getStatus()
        );
        blackListHistoryRepository.save(history);
    }

    @Override
    public Long update(Long id, BlackListUpdateRequestDto dto) {
        BlackList blacklist = blacklistRepository.findById(id)
                .orElseThrow(() -> new NotFoundException(MEMBER_NOT_FOUND));

        blacklist.updateReason(dto.getReason());
        BlackListHistory blackListHistory = BlackListHistory.create(UPDATE,blacklist,blacklist.getHashValue(), blacklist.getType(), blacklist.getReason(), blacklist.getStatus());
        blackListHistoryRepository.save(blackListHistory);

        return blacklist.getId();

    }

    @Override
    public GlobalResultDto delete(Long id) {

        BlackList blacklist = blacklistRepository.findById(id)
                .orElseThrow(() -> new NotFoundException(MEMBER_NOT_FOUND));

        // 히스토리엔 저
        BlackListHistory blackListHistory = BlackListHistory.create(BlacklistAction.DELETE,blacklist,blacklist.getHashValue(), blacklist.getType(), blacklist.getReason(), BlacklistStatus.EXPIRED);
        blackListHistoryRepository.save(blackListHistory);
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
