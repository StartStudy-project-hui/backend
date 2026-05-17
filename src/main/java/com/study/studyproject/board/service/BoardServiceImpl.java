package com.study.studyproject.board.service;

import com.study.studyproject.board.domain.Board;
import com.study.studyproject.board.dto.BoardOneResponseDto;
import com.study.studyproject.board.dto.BoardReUpdateRequestDto;
import com.study.studyproject.board.dto.BoardWriteRequestDto;
import com.study.studyproject.board.repository.BoardRepository;
import com.study.studyproject.global.GlobalResultDto;
import com.study.studyproject.global.auth.UserDetailsImpl;
import com.study.studyproject.global.exception.ex.ForbiddenException;
import com.study.studyproject.global.exception.ex.NotFoundException;
import com.study.studyproject.login.domain.Role;
import com.study.studyproject.member.domain.Member;
import com.study.studyproject.member.repository.MemberRepository;
import com.study.studyproject.postlike.domain.PostLike;
import com.study.studyproject.postlike.dto.PostLikeOneResponseDto;
import com.study.studyproject.postlike.repository.PostLikeRepository;
import com.study.studyproject.postlike.service.PostLikeService;
import com.study.studyproject.reply.domain.Reply;
import com.study.studyproject.reply.repository.ReplyRepository;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.*;

import static com.study.studyproject.board.domain.Board.validateDeleteBoard;
import static com.study.studyproject.global.exception.ex.ErrorCode.*;
import static com.study.studyproject.login.domain.Role.*;
import static com.study.studyproject.login.domain.Role.isAdmin;
import static com.study.studyproject.login.domain.Role.isAnonymous;

@Transactional
@RequiredArgsConstructor
@Service
@Slf4j
public class BoardServiceImpl implements BoardService {

    private final BoardRepository boardRepository;
    private final ReplyRepository replyRepository;
    private final MemberRepository memberRepository;
    private final PostLikeRepository postLikeRepository;

    //작성
    @Override
    public GlobalResultDto boardSave(BoardWriteRequestDto boardWriteRequestDto, Long memberId) {
        Member member = findMemberById(memberId);
        Board entity = boardWriteRequestDto.toEntity(member);
        boardRepository.save(entity);
        return new GlobalResultDto("글 작성 완료", HttpStatus.OK.value());

    }

    private Member findMemberById(Long memberId) {
        return memberRepository.findById(memberId).orElseThrow(() -> new NotFoundException(NOT_FOUND_BOARD));
    }

    //수정
    @Override
    public GlobalResultDto updateWrite(BoardReUpdateRequestDto boardReUpdateRequestDto) {
        Board board = findBoardById(boardReUpdateRequestDto.getBoardId());
        board.updateBoard(boardReUpdateRequestDto);
        return new GlobalResultDto("글 작성 완료", HttpStatus.OK.value());
    }


    //글 1개만 가져오기
    @Override
    public BoardOneResponseDto detailBoard(Long boardId) {
        Board board = findBoardById(boardId);
        return BoardOneResponseDto.of(board);

    }


    @Override
    @Transactional
    public GlobalResultDto boardDeleteOne(Long boardId, UserDetailsImpl userDetails) {
        Board board = findBoardById(boardId);
        validateBoardOwner(board, userDetails);
        List<Reply> replies = replyRepository.findByBoardReplies(boardId);
        List<PostLike> postLikes = postLikeRepository.findByBoardId(boardId);

        validateDeleteBoard(replies, postLikes);

        boardRepository.delete(board);

        return new GlobalResultDto("게시글 삭제 완료", HttpStatus.OK.value());
    }

    private void validateBoardOwner(Board board, UserDetailsImpl userDetails) {
        if (isAnonymous()) {
            throw new ForbiddenException(NOT_FOUND_MEMBER);
        }

        if (!isBoardOwner(board, userDetails.getMemberId())) {
            throw new ForbiddenException(UNABLE_DELETE_BOARD);
        }    }

    private boolean isBoardOwner(Board board, Long loginMemberId) {
        return board.getMember().getId().equals(loginMemberId);
    }

    private Board findBoardById(Long boardId) {
        return boardRepository.findById(boardId).orElseThrow(() -> new NotFoundException(NOT_FOUND_BOARD));
    }



    @Transactional
    public void updateView(Long boardId){
        validateBoardExists(boardId);
        boardRepository.updateHits(boardId);
    }


    private void validateBoardExists(Long boardId) {
        if (boardRepository.existsById(boardId)) {
            return;
        }
        throw new NotFoundException(NOT_FOUND_BOARD);
    }



    //모집 구분 변경
    @Override
    public GlobalResultDto changeRecruit(Long boardId) {
        Board board = findBoardById(boardId);
        board.changeRecuritBoard();
        return new GlobalResultDto("모집 구분 변경 완료", HttpStatus.OK.value());
    }
}
