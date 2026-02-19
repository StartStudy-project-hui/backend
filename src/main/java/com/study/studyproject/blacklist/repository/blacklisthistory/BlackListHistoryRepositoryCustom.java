package com.study.studyproject.blacklist.repository.blacklisthistory;

import com.study.studyproject.blacklist.dto.request.BlackListHistoryMainRequestDto;
import com.study.studyproject.blacklist.dto.response.BlacklistHistoryResponseDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;

public interface BlackListHistoryRepositoryCustom {
    Page<BlacklistHistoryResponseDto>  blackListHistorySearchPageMainList(BlackListHistoryMainRequestDto blackListHistoryMainRequestDto, Pageable pageable);
    Slice<BlacklistHistoryResponseDto> searchSliceByEmail(String email, Pageable pageable);


}
