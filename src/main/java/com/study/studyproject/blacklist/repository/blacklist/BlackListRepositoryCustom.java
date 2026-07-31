package com.study.studyproject.blacklist.repository.blacklist;

import com.study.studyproject.blacklist.dto.request.BlackListMainRequestDto;
import com.study.studyproject.blacklist.dto.response.BlacklistResponseDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface BlackListRepositoryCustom {
    Page<BlacklistResponseDto> blackListSearchPageMainList(BlackListMainRequestDto blackListMainRequestDto, String hashedEmail, Pageable pageable);


}
