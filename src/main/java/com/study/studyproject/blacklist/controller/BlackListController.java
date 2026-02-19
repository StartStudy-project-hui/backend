package com.study.studyproject.blacklist.controller;

import com.study.studyproject.blacklist.dto.request.BlackListCreateRequestDto;
import com.study.studyproject.blacklist.dto.request.BlackListUpdateRequestDto;
import com.study.studyproject.blacklist.dto.request.BlackListMainRequestDto;
import com.study.studyproject.blacklist.dto.response.BlacklistResponseDto;
import com.study.studyproject.blacklist.service.BlackListService;
import com.study.studyproject.global.GlobalResultDto;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/back-list/admin")
@Tag(name = "블랙리스트 API", description = "블랙리스트 Document")
@Slf4j
public class BlackListController {
    private final BlackListService blackListService;

    // 등록
    @PostMapping
    public ResponseEntity<GlobalResultDto> register(@RequestBody BlackListCreateRequestDto dto) {
        return ResponseEntity.ok(blackListService.register(dto));
    }

    // 수정
    @PatchMapping("/{id}")
    public ResponseEntity<GlobalResultDto>  update(@PathVariable Long id,
                                         @RequestBody BlackListUpdateRequestDto dto) {
        return ResponseEntity.ok(blackListService.update(id, dto));
    }

    // 삭제(해제)
    @DeleteMapping("/{id}")
    public ResponseEntity<GlobalResultDto> delete(@PathVariable Long id) {
        return ResponseEntity.ok(blackListService.delete(id));
    }

    // 조회 - 페이징 처리, 해당 닉네임
    @GetMapping
    public ResponseEntity<Page<BlacklistResponseDto>> findPageAllBlackList(
            @ModelAttribute BlackListMainRequestDto condition,
            @PageableDefault(size = 15) Pageable pageable) {

        return ResponseEntity.ok(blackListService.findPageBlackList(condition,pageable));
    }

}
