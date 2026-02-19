package com.study.studyproject.blacklist.service;

import com.study.studyproject.blacklist.dto.request.BlackListCreateRequestDto;
import com.study.studyproject.blacklist.dto.request.BlackListMainRequestDto;
import com.study.studyproject.blacklist.dto.request.BlackListUpdateRequestDto;
import com.study.studyproject.blacklist.dto.response.BlacklistResponseDto;
import com.study.studyproject.global.GlobalResultDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface BlackListService {
    GlobalResultDto register(BlackListCreateRequestDto dto);

    GlobalResultDto update(Long id, BlackListUpdateRequestDto dto);

    GlobalResultDto delete(Long id);

    Page<BlacklistResponseDto> findPageBlackList(BlackListMainRequestDto blackListMainRequestDto, Pageable pageable);



}
