package com.study.studyproject.blacklist.service;

import com.study.studyproject.blacklist.dto.request.BlackListHistoryMainRequestDto;
import com.study.studyproject.blacklist.dto.response.BlacklistHistoryResponseDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;

public interface BlackListHistoryService {


    // 특정 아이디  히스토리 조회
    Page<BlacklistHistoryResponseDto> blackHistoryAdminList(BlackListHistoryMainRequestDto blackListHistoryMainRequestDto, Pageable pageable);

    Slice<BlacklistHistoryResponseDto> findSliceBlackHistoryById(Long id, Pageable pageable);

}
