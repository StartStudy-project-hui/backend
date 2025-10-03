package com.study.studyproject.reply.service;

import com.study.studyproject.board.repository.BoardRepository;
import com.study.studyproject.board.domain.Board;
import com.study.studyproject.member.domain.Member;
import com.study.studyproject.reply.domain.Reply;
import com.study.studyproject.global.exception.ex.NotFoundException;
import com.study.studyproject.member.repository.MemberRepository;
import com.study.studyproject.reply.dto.ReplyInfoResponseDto;
import com.study.studyproject.reply.dto.ReplyRequestDto;
import com.study.studyproject.reply.dto.ReplyResponseDto;
import com.study.studyproject.reply.dto.UpdateReplyRequest;
import com.study.studyproject.reply.repository.ReplyRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.study.studyproject.global.exception.ex.ErrorCode.*;
import static com.study.studyproject.reply.dto.ReplyInfoResponseDto.convertReplyToDto;
import static com.study.studyproject.reply.dto.ReplyResponseDto.ReplyResponseToDto;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class ReplyServiceImpl implements ReplyService {


    private final BoardRepository boardRepository;
    private final ReplyRepository replyRepository;
    private final MemberRepository memberRepository;


    public ReplyResponseDto getRepliesForOneBoard(Long boardId) {
        List<Reply> comments = replyRepository.findByBoardReply(boardId);
        List<ReplyInfoResponseDto> commentResponseDTOList = getReplyInfoResponseDtos(comments);
        return ReplyResponseToDto(comments.size(), commentResponseDTOList);
    }

    private static List<ReplyInfoResponseDto> getReplyInfoResponseDtos(List<Reply> comments) {

        List<ReplyInfoResponseDto> commentResponseDTOList = new ArrayList<>();
        Map<Long, ReplyInfoResponseDto> commentDTOHashMap = new HashMap<>();


        comments.forEach(c -> {
            ReplyInfoResponseDto commentResponseDTO = convertReplyToDto(c);
            commentDTOHashMap.put(commentResponseDTO.getReplyId(), commentResponseDTO);
            if (c.getParent() != null) // 자식일 경우
                commentDTOHashMap.get(c.getParent().getId()).getChildren().add(commentResponseDTO);
            else commentResponseDTOList.add(commentResponseDTO); // 부모일 경우
        });

        return commentResponseDTOList;
    }


    @Transactional
    public void insert(Long memberId, ReplyRequestDto replyRequestDto) {
        Board board = findByBoardId(replyRequestDto);
        Member member = findByMemberId(memberId);
        Reply reply = Reply.toEntity(replyRequestDto, board, member);

        if (replyRequestDto.isReplyParent()) { // 대댓글인 경우
            Reply replyParent = findByReply(replyRequestDto);
            reply.updateParent(replyParent);
        }

//문제
        reply.UpdateBoard(board);
        reply.updateWriter(member);


        replyRepository.save(reply);
    }

    private Reply findByReply(ReplyRequestDto replyRequestDto) {
        return replyRepository.findById(replyRequestDto.getParentId())
                .orElseThrow(() -> new NotFoundException(NOT_FOUND_REPLY));
    }

    private Member findByMemberId(Long memberId) {
        return memberRepository
                .findById(memberId)
                .orElseThrow(() -> new NotFoundException(NOT_FOUND_MEMBER));
    }

    private Board findByBoardId(ReplyRequestDto replyRequestDto) {
        return boardRepository.findById(replyRequestDto.getBoardId())
                .orElseThrow(() -> new NotFoundException(NOT_FOUND_BOARD));
    }


    @Override
    public void updateReply(UpdateReplyRequest updateReplyRequest) {
        Reply findReply = replyRepository.findById(updateReplyRequest.getReplyId()).orElseThrow(() -> new NotFoundException(NOT_FOUND_REPLY));
        findReply.updateReply(updateReplyRequest.getContent());
    }


    @Override
    public void deleteReply(Long num) { //댓글 num
        Reply reply = replyRepository.findCommentByIdWithParent(num)
                .orElseThrow(() -> new NotFoundException(NOT_FOUND_REPLY));
        if (reply.hasChildrenReplies()) { //자식이 있는 상태
            reply.ChangeIsDeleted(true);
        } else { //삭제 가능한 조상 댓글
            replyRepository.delete(getDelete(reply));
        }

    }


    private Reply getDelete(Reply reply) {
        Reply parent = reply.getParent();
        if (isDeleteReply(parent)) {
            return getDelete(parent);
        }
        return reply;
    }

    private static boolean isDeleteReply(Reply parent) {
        return parent != null && parent.getChildren().size() == 1 && parent.getIsDeleted();
    }


}
