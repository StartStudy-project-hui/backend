package com.study.studyproject.postlike.controller;


import com.study.studyproject.global.GlobalResultDto;
import com.study.studyproject.global.auth.CurrentUser;
import com.study.studyproject.global.auth.UserDetailsImpl;
import com.study.studyproject.login.domain.Role;
import com.study.studyproject.postlike.dto.PostLikeOneResponseDto;
import com.study.studyproject.postlike.service.PostLikeService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;


@RestController
@RequiredArgsConstructor
@RequestMapping("/api/")
@Tag(name = "사용자 관심 글",description = "사용장 관심 글 기능 수정 및 삭제")
@Slf4j
public class PostLikeController {

    private final PostLikeService postLikeService;

    @Operation(summary = "관심 여부", description = "게시글 관심 조회")
    @GetMapping("v1/view/post-like/{boardId}")
    public ResponseEntity<PostLikeOneResponseDto> postLikeView(@PathVariable("boardId") Long boardId, @Parameter(hidden = true) @CurrentUser UserDetailsImpl userDetails) {
        if(Role.isAnonymous()){
            return null;
        }
        return ResponseEntity.ok(postLikeService.getPostLikeForOneBoard(userDetails.getMember(), boardId));

    }


    @Operation(summary = "관심글 등록", description = "사용자의 관심글 등록합니다.")
    @PostMapping("v1/post-like/{boardId}")
    public ResponseEntity<GlobalResultDto> postLikeSave(@PathVariable("boardId") Long boardId, @Parameter(hidden = true) @CurrentUser UserDetailsImpl userDetails) {
        log.info("들어옴");
        return ResponseEntity.ok(postLikeService.postLikeSave(boardId,userDetails.getMember()));
    }


    @Operation(summary = "관심글 삭제", description = "사용자의 관심글 삭제합니다.")
    @DeleteMapping("v1/post-like/{postLikeId}")
    public ResponseEntity<GlobalResultDto> postLikeDelete(@PathVariable(name = "postLikeId") Long postLikeId, @Parameter(hidden = true) @CurrentUser UserDetailsImpl userDetails) {
        return ResponseEntity.ok(postLikeService.postLikeDelete(postLikeId, userDetails.getMember()));
    }



}
