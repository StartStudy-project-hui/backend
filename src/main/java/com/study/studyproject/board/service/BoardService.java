package com.study.studyproject.board.service;

import com.study.studyproject.board.dto.BoardOneResponseDto;
import com.study.studyproject.board.dto.BoardReUpdateRequestDto;
import com.study.studyproject.board.dto.BoardWriteRequestDto;
import com.study.studyproject.login.domain.Role;
import com.study.studyproject.global.GlobalResultDto;
import com.study.studyproject.global.auth.UserDetailsImpl;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

public interface BoardService {
    GlobalResultDto boardSave(BoardWriteRequestDto boardWriteRequestDto, Long memberId);

     GlobalResultDto updateWrite(BoardReUpdateRequestDto boardReUpdateRequestDto);

    void updateView(Long boardId);
     BoardOneResponseDto detailBoard(Long boardId);


     GlobalResultDto boardDeleteOne(Long boardId, UserDetailsImpl userDetails);

     GlobalResultDto changeRecruit(Long boardId);

}
