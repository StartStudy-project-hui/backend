package com.study.studyproject.main.controller;

import com.study.studyproject.main.dto.ListResponseDto;
import com.study.studyproject.main.dto.MainRequestDto;
import com.study.studyproject.main.service.ListService;
import io.swagger.v3.oas.annotations.Parameter;
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
@Tag(name = "메인 기능 구현")
@Slf4j
public class MainController {

    private final ListService listService;

    @GetMapping("/api/v1/")
    public ResponseEntity<Page<ListResponseDto>> mainList(
            @RequestParam(value = "title",required = false) String title,
             MainRequestDto mainRequestDto,
            @Parameter(hidden = true) @PageableDefault(size = 10) Pageable pageable) {
        Page<ListResponseDto> list = listService.list(title, mainRequestDto, pageable);
        return ResponseEntity.ok(list);
    }


}
