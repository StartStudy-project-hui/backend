package com.study.studyproject.board.service;

import com.study.studyproject.board.domain.Board;
import com.study.studyproject.board.dto.BoardOneResponseDto;
import com.study.studyproject.board.dto.BoardReUpdateRequestDto;
import com.study.studyproject.board.dto.BoardWriteRequestDto;
import com.study.studyproject.board.repository.BoardRepository;
import com.study.studyproject.global.GlobalResultDto;
import com.study.studyproject.global.auth.UserDetailsImpl;
import com.study.studyproject.global.exception.ex.NotFoundException;
import com.study.studyproject.login.domain.Role;
import com.study.studyproject.member.domain.Member;
import com.study.studyproject.member.repository.MemberRepository;
import com.study.studyproject.postlike.domain.PostLike;
import com.study.studyproject.postlike.repository.PostLikeRepository;
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

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.*;

import static com.study.studyproject.board.domain.Board.validateDeleteBoard;
import static com.study.studyproject.global.exception.ex.ErrorCode.BOARD_NOT_FOUND;
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
    private final static String VIEWCOOKIENAME = "alreadyViewCookie";

    //작성
    @Override
    public GlobalResultDto boardSave(BoardWriteRequestDto dto, Long memberId) {
        Member member = findMemberById(memberId);
        Board board = Board.create(
                member,
                dto.getTitle(),
                dto.getContent(),
                dto.getCategory(),
                dto.getConnectionType(),
                dto.getOfflineLocation()
        );

        boardRepository.save(board);
        return new GlobalResultDto("글 작성 완료", HttpStatus.OK.value());

    }

    private Member findMemberById(Long memberId) {
        return memberRepository.findById(memberId).orElseThrow(() -> new NotFoundException(BOARD_NOT_FOUND));
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
    public BoardOneResponseDto boardOne(Long boardId, UserDetailsImpl userDetails) {
        Board board = findBoardById(boardId);
        if (isAnonymous()) {
            return BoardOneResponseDto.of(board, null);
        }
        return BoardOneResponseDto.of(board, userDetails.getNickname());

    }


    private Board findBoardById(Long boardId) {
        return boardRepository.findById(boardId).orElseThrow(() -> new NotFoundException(BOARD_NOT_FOUND));
    }



    @Transactional
    public void updateView(Long boardId,HttpServletRequest request, HttpServletResponse response){
        validateBoardExists(boardId);
        if(isNewView(boardId, request)){
            Cookie newCookie = createCookieForForNotOverlap(boardId);
            response.addCookie(newCookie);
            boardRepository.updateHits(boardId);
        }
    }

    private void validateBoardExists(Long boardId) {
        if (boardRepository.existsById(boardId)) {
            return;
        }
        throw new NotFoundException(BOARD_NOT_FOUND);
    }

    private boolean isNewView(Long boardId, HttpServletRequest request) {
        Cookie[] cookies = request.getCookies();
        if (cookies != null) {
            for (Cookie cookie : cookies) {
                if (cookie.getName().equals(VIEWCOOKIENAME + boardId)) {
                    return false;  // 이미 조회한 경우
                }
            }
        }
        return true;  // 조회하지 않은 경우

    }

    private Cookie createCookieForForNotOverlap(Long postId) {
        Cookie cookie = new Cookie(VIEWCOOKIENAME + postId, String.valueOf(postId));
        cookie.setMaxAge(getRemainSecondForTomorrow());
        cookie.setHttpOnly(true);
        return cookie;
    }

    private int getRemainSecondForTomorrow() {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime tommorow = LocalDateTime.now().plusDays(1L).truncatedTo(ChronoUnit.DAYS);
        return (int) now.until(tommorow, ChronoUnit.SECONDS);
    }


    @Override
    public GlobalResultDto boardDeleteOne(Long boardId, Role role) {
        if (isAdmin(role)) {
            Board board = findBoardById(boardId);
            board.changeAdminDeleteBoard();
            return new GlobalResultDto("관리자 권한으로 게시글 삭제 완료", HttpStatus.OK.value());
        }

        //댓글
        List<Reply> replies = replyRepository.findByBoardReplies(boardId);
        //postLike
        List<PostLike> postLikes = postLikeRepository.findByBoardId(boardId);


        validateDeleteBoard(replies, postLikes);

        boardRepository.deleteById(boardId);
        return new GlobalResultDto("게시글 삭제 완료", HttpStatus.OK.value());
    }


    //모집 구분 변경
    @Override
    public GlobalResultDto changeRecruit(Long boardId) {
        Board board = findBoardById(boardId);
        board.changeRecuritBoard();
        return new GlobalResultDto("모집 구분 변경 완료", HttpStatus.OK.value());
    }
}
