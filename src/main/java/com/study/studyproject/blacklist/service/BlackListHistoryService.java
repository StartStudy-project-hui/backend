package com.study.studyproject.blacklist.service;

import com.study.studyproject.blacklist.dto.request.BlackListHistoryMainRequestDto;
import com.study.studyproject.blacklist.dto.response.BlacklistHistoryAdminResponseDto;
import com.study.studyproject.blacklist.dto.response.BlacklistHistoryMemberResponseDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;

public interface BlackListHistoryService {


    // 특정 아이디  히스토리 조회
    Page<BlacklistHistoryAdminResponseDto> blackHistoryAdminList(BlackListHistoryMainRequestDto blackListHistoryMainRequestDto, Pageable pageable);

    Slice<BlacklistHistoryMemberResponseDto> findSliceBlackHistoryById(Long id, Pageable pageable);

}
