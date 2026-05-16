package com.study.studyproject.member.controller;

import com.study.studyproject.board.service.BoardService;
import com.study.studyproject.global.auth.CurrentUser;
import com.study.studyproject.login.domain.Role;
import com.study.studyproject.global.GlobalResultDto;
import com.study.studyproject.global.auth.UserDetailsImpl;
import com.study.studyproject.member.dto.AdminDashBoardResponseDto;
import com.study.studyproject.member.dto.MemberListRequestDto;
import com.study.studyproject.member.dto.UserInfoResponseDto;
import com.study.studyproject.member.service.AdminServiceImpl;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/")
@Tag(name = "관리자 기능",description = "사용자 전체조회와 관리자 정보 조회")
public class AdminController {

    private final AdminServiceImpl adminService;
    private final BoardService boardService;


    @GetMapping("v1/admin/user-all")
    @Operation(summary = "전체 사용자 조회", description = "사용자 검색 조회 및 사용자 전체 조회 가능")
    public ResponseEntity<Page<UserInfoResponseDto>> userAllInfo(
            @RequestParam(required = false,value = "username") String username,
            @Parameter(hidden = true) @PageableDefault(size = 10) Pageable pageable) {
        return ResponseEntity.ok(adminService.userInfoAll(username,pageable));
    }

    @GetMapping("v1/admin/dash-board")
    @Operation(summary = "관리자 대시보드 ", description = "관리자 정보와 게시글 정보 조회")
    public ResponseEntity<AdminDashBoardResponseDto> adminDashInfo(            @Parameter(hidden = true) @CurrentUser UserDetailsImpl userDetails,
                                                   MemberListRequestDto memberListRequestDto, @Parameter(hidden = true) @PageableDefault(size = 10) Pageable pageable) {
        return ResponseEntity.ok(adminService.adminDashBoardInfo(userDetails.getMember(), memberListRequestDto, pageable));
    }

    @DeleteMapping("v1/admin/board/{boardId}")
    @Operation(summary = "관리자 게시글 삭제", description = "관리자 권한으로 게시글을 삭제 처리합니다.")
    public ResponseEntity<GlobalResultDto> deleteBoardByAdmin(
            @PathVariable Long boardId
    ) {
        return ResponseEntity.ok(adminService.adminDeleteBoard(boardId));
    }
}
