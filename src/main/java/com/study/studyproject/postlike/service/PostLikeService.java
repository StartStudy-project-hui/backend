package com.study.studyproject.postlike.service;


import com.study.studyproject.board.repository.BoardRepository;
import com.study.studyproject.board.domain.Board;
import com.study.studyproject.member.domain.Member;
import com.study.studyproject.postlike.domain.PostLike;
import com.study.studyproject.global.GlobalResultDto;
import com.study.studyproject.global.exception.ex.BadRequestException;
import com.study.studyproject.global.exception.ex.ForbiddenException;
import com.study.studyproject.global.exception.ex.NotFoundException;
import com.study.studyproject.postlike.domain.PostLikeState;
import com.study.studyproject.postlike.dto.PostLikeOneResponseDto;
import com.study.studyproject.postlike.repository.PostLikeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

import static com.study.studyproject.global.exception.ex.ErrorCode.*;

@Service
@Transactional
@RequiredArgsConstructor
public class PostLikeService {
    private final PostLikeRepository postLikeRepository;
    private final BoardRepository boardRepository;


    @Transactional(readOnly = true)
    public PostLikeOneResponseDto getPostLikeForOneBoard(Member member, Long boardId) {
        Board board = findByBoardId(boardId);
        Optional<PostLike> postLike = postLikeRepository.findByBoardAndMember(board, member);

        Long postLikeId = postLike.map(PostLike::getId).orElse(null);
        String postLikeValue = postLike.isPresent() ? PostLikeState.LIKING.getName() : PostLikeState.LIKE.getName();
        return PostLikeOneResponseDto.of(postLikeValue, postLikeId);
    }

    private Board findByBoardId(Long boardId) {
        return boardRepository.findById(boardId).orElseThrow(() -> new NotFoundException(NOT_FOUND_BOARD));
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

    public GlobalResultDto postLikeDelete(Long postLikeId, Member member) {
        PostLike postLike = postLikeRepository.findById(postLikeId)
                .orElseThrow(() -> new NotFoundException(NOT_FOUND_VALUE));
        if (!postLike.getMember().getId().equals(member.getId())) {
            throw new ForbiddenException(UNABLE_ACCESS);
        }
        postLikeRepository.deleteById(postLikeId);
        return new GlobalResultDto("관심글이 삭제되었습니다.", HttpStatus.OK.value());
    }


}
