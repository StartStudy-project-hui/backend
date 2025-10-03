package com.study.studyproject.postlike.service;


import com.study.studyproject.board.repository.BoardRepository;
import com.study.studyproject.board.domain.Board;
import com.study.studyproject.member.domain.Member;
import com.study.studyproject.postlike.domain.PostLike;
import com.study.studyproject.global.GlobalResultDto;
import com.study.studyproject.global.exception.ex.BadRequestException;
import com.study.studyproject.global.exception.ex.NotFoundException;
import com.study.studyproject.postlike.domain.PostLikeState;
import com.study.studyproject.postlike.dto.PostLikeOneResponseDto;
import com.study.studyproject.postlike.repository.PostLikeRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.Optional;

import static com.study.studyproject.global.exception.ex.ErrorCode.*;

@Service
@Transactional
@RequiredArgsConstructor
public class PostLikeService {
    private final PostLikeRepository postLikeRepository;
    private final BoardRepository boardRepository;


    public PostLikeOneResponseDto getPostLikeForOneBoard(Member member, Long boardId) {
        Board board = findByBoardId(boardId);
        Long postLikeId = findByPostLikeId(member, boardId);
        String  postLikeValue = findByPostLike(member, board);
        return PostLikeOneResponseDto.of(postLikeValue,postLikeId);
    }

    private Board findByBoardId(Long boardId) {
        return boardRepository.findById(boardId).orElseThrow(() -> new NotFoundException(NOT_FOUND_BOARD));
    }

    public Long findByPostLikeId(Member member, long boardId) {
        Board board = findByBoardId(boardId);
        Optional<PostLike> byBoardAndMember = postLikeRepository.findByBoardAndMember(board, member);
        if (byBoardAndMember.isPresent()) {
            return byBoardAndMember.get().getId();
        }
        return null;
    }

    private String findByPostLike(Member member, Board board) {
            Optional<PostLike> byBoardAndMember = postLikeRepository.findByBoardAndMember(board, member);
            return byBoardAndMember.isPresent() ? PostLikeState.LIKING.getName() : PostLikeState.LIKE.getName();
    }


    public GlobalResultDto postLikeSave(Long boardId ,Member member) {
        Board board = findByBoardId(boardId);
        Optional<PostLike> postLike = postLikeRepository.findByBoardAndMember(board, member);
        if (postLike.isPresent()) {
            throw new BadRequestException(POST_LIKE_DUPLICATED);
        }
        postLikeRepository.save(PostLike.create(member, board));
        return new GlobalResultDto("관심글이 추가되었습니다.", HttpStatus.OK.value());
    }

    public GlobalResultDto postLikeDelete(Long postLikeId) {
        postLikeRepository.deleteById(postLikeId);
        return new GlobalResultDto("관심글이 삭제되었습니다.", HttpStatus.OK.value());
    }


}
