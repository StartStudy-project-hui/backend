package com.study.studyproject.blacklist.repository.blacklisthistory;

import com.study.studyproject.blacklist.dto.request.BlackListHistoryMainRequestDto;
import com.study.studyproject.blacklist.dto.response.BlacklistHistoryAdminResponseDto;
import com.study.studyproject.blacklist.dto.response.BlacklistHistoryMemberResponseDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;

public interface BlackListHistoryRepositoryCustom {
    Page<BlacklistHistoryAdminResponseDto>  blackListHistorySearchPageMainList(BlackListHistoryMainRequestDto blackListHistoryMainRequestDto, Pageable pageable);
    Slice<BlacklistHistoryMemberResponseDto> searchSliceByEmail(String email, Pageable pageable);


}
