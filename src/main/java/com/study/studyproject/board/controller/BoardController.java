
package com.study.studyproject.board.controller;

import com.study.studyproject.board.dto.BoardOneResponseDto;
import com.study.studyproject.board.dto.BoardReUpdateRequestDto;
import com.study.studyproject.board.dto.BoardWriteRequestDto;
import com.study.studyproject.board.service.BoardService;
import com.study.studyproject.global.GlobalResultDto;
import com.study.studyproject.global.auth.CurrentUser;
import com.study.studyproject.global.auth.UserDetailsImpl;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/board/")
@Tag(name = "게시판 API", description = "게시판 Document")
@Slf4j
public class BoardController {

    private final BoardService boardService;

    //글쓰기 수정
    @PatchMapping("member")
    @Operation(summary = "글쓰기 수정", description = "글쓰기 수정 기능")
    public ResponseEntity<GlobalResultDto> updateWriting(@RequestBody @Validated BoardReUpdateRequestDto boardReUpdateRequestDto) {
        return ResponseEntity.ok(boardService.updateWrite(boardReUpdateRequestDto));

    }

    @PostMapping("member")
    @Operation(summary = "글쓰기 작성", description = "글쓰기 작성")
    public ResponseEntity<GlobalResultDto> writing(@RequestBody @Validated BoardWriteRequestDto boardWriteRequestDto, @Parameter(hidden = true) @CurrentUser UserDetailsImpl userDetails) {
        return ResponseEntity.ok(boardService.boardSave(boardWriteRequestDto, userDetails.getMemberId()));

    }

    @Operation(summary = "모집구분 변경", description = "모집구분 변경")
    @PatchMapping("/member/recruit/{boardId}")
    public ResponseEntity<GlobalResultDto> changeRecruit(@PathVariable(name = "boardId") Long boardId) {
        return ResponseEntity.ok(boardService.changeRecruit(boardId));
    }


    //삭제
    @DeleteMapping("member/{boardId}")
    @Operation(summary = "게시글 삭제 ", description = "해당 게시글 삭제")
    public ResponseEntity<GlobalResultDto> deleteBoard(@PathVariable(name = "boardId", required = true) Long boardId) {
        return ResponseEntity.ok(boardService.boardDeleteOne(boardId, null));
    }


    //글 조회 1개 -
    @GetMapping("/{boardId}")
    @Operation(summary = "게시글 상세", description = "게시글 상세페이지")
    public ResponseEntity<BoardOneResponseDto> getBoard(@Parameter(description = "게시판 ID") @PathVariable(name = "boardId") Long boardId,
                                                        @Parameter(hidden = true) @CurrentUser UserDetailsImpl userDetails
            , HttpServletRequest request,HttpServletResponse response

    ) {
        boardService.updateView(boardId, request, response);
        BoardOneResponseDto boardOneResponseDto = boardService.boardOne(boardId, userDetails);
        return ResponseEntity.ok(boardOneResponseDto);

    }


}
