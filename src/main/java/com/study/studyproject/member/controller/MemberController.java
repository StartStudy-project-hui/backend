package com.study.studyproject.member.controller;

import com.study.studyproject.global.auth.CurrentUser;
import com.study.studyproject.global.auth.UserDetailsImpl;
import com.study.studyproject.list.dto.ListResponseDto;
import com.study.studyproject.global.GlobalResultDto;
import com.study.studyproject.member.dto.MemberListRequestDto;
import com.study.studyproject.member.dto.MemberUpdateResDto;
import com.study.studyproject.member.dto.UserInfoResponseDto;
import com.study.studyproject.member.service.MemberServiceImpl;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/user")
@Tag(name = "마이페이지 기능", description = "사용자 마이페이지 기능 구현 ")
public class MemberController {


    private final MemberServiceImpl memberService;

    //사용자 정보 조회
    @Operation(summary = "사용자 정보 조회", description = "자신의 사용자 정보를 조회")
    @GetMapping("/info")
    public ResponseEntity<UserInfoResponseDto> userInfo(@Parameter(hidden = true) @CurrentUser UserDetailsImpl userDetails) {
        return ResponseEntity.ok(memberService.userInfoService(userDetails.getMember()));
    }

    //수정
    @Operation(summary = "사용자 정보 수정", description = "사용자 정보 수정 기능")
    @PatchMapping("/info")
    public ResponseEntity<GlobalResultDto> userInfoUpdate(@RequestBody @Validated MemberUpdateResDto memberUpdateResDto,@Parameter(hidden = true) @CurrentUser UserDetailsImpl userDetails ) {
        return ResponseEntity.ok(memberService.userInfoUpdate(userDetails.getMember(), memberUpdateResDto));
    }


    @Operation(summary = "내가 작성한 게시글 조회", description = "사용자 스터디 게시글 조회")
    @GetMapping("/lists")
    public ResponseEntity<Page<ListResponseDto>> mainList(
            @Parameter(hidden = true) @CurrentUser UserDetailsImpl userDetails,
             MemberListRequestDto memberListRequestDto,
            @Parameter(hidden = true) @PageableDefault(size = 10) Pageable pageable) {

        return ResponseEntity.ok(memberService.listMember(userDetails.getMemberId(), memberListRequestDto, pageable));
    }



    @Operation(summary = "관심 게시글 조회", description = "사용자 관심 게시글 조회")
    @GetMapping("/post-likes")
    public ResponseEntity<Page<ListResponseDto>> userPostLike(
            @Parameter(hidden = true) @CurrentUser UserDetailsImpl userDetails,
            MemberListRequestDto memberListRequestDto,
            @Parameter(hidden = true) @PageableDefault(size = 10) Pageable pageable) {

        return ResponseEntity.ok(memberService.postLikeBoard(userDetails.getMemberId(), memberListRequestDto, pageable));
    }




}
