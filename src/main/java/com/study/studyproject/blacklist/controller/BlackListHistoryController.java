package com.study.studyproject.blacklist.controller;

import com.study.studyproject.blacklist.dto.request.BlackListHistoryMainRequestDto;
import com.study.studyproject.blacklist.dto.response.BlacklistHistoryAdminResponseDto;
import com.study.studyproject.blacklist.dto.response.BlacklistHistoryMemberResponseDto;
import com.study.studyproject.blacklist.service.BlackListHistoryService;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/back-list-history/")
@Tag(name = "블랙리스트 히스토리 API", description = "블랙리스트 Document")
@Slf4j
public class BlackListHistoryController {
    private final BlackListHistoryService blackListHistoryService;

    // 전체 조회 , 특정 값 조회
    @GetMapping("admin/")
    public ResponseEntity<Page<BlacklistHistoryAdminResponseDto>> findPageBlackListHistory(
            @ModelAttribute BlackListHistoryMainRequestDto blackListHistoryMainRequestDto
            , @PageableDefault(size = 15) Pageable pageable
    ) {
        Page<BlacklistHistoryAdminResponseDto> responseDto = blackListHistoryService.blackHistoryAdminList(blackListHistoryMainRequestDto, pageable);
        return ResponseEntity.ok(responseDto);
    }


    // 사용자가 조회시
    @GetMapping("{id}")
    public Slice<BlacklistHistoryMemberResponseDto> findSliceBlackListHistoryById(@PathVariable Long id,
                                                                                  @PageableDefault(size = 15) Pageable pageable) {
        return blackListHistoryService.findSliceBlackHistoryById(id, pageable);

    }
}


