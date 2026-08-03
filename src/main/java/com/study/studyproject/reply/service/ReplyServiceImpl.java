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

        // 1차: 모든 댓글을 먼저 DTO로 변환해 맵에 등록 (자식이 부모보다 먼저 처리되어도 안전하도록)
        comments.forEach(c -> {
            ReplyInfoResponseDto commentResponseDTO = convertReplyToDto(c);
            commentDTOHashMap.put(commentResponseDTO.getReplyId(), commentResponseDTO);
        });

        // 2차: 부모-자식 관계 연결. 부모가 목록에 없는 경우(데이터 불일치)에는 최상위로 취급
        comments.forEach(c -> {
            ReplyInfoResponseDto commentResponseDTO = commentDTOHashMap.get(c.getId());
            ReplyInfoResponseDto parentDTO = c.getParent() != null
                    ? commentDTOHashMap.get(c.getParent().getId())
                    : null;

            if (parentDTO != null) {
                parentDTO.getChildren().add(commentResponseDTO);
            } else {
                commentResponseDTOList.add(commentResponseDTO);
            }
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
            reply.markAsDeleted();
            return;
        }

        Reply deleteTarget = findDeleteTarget(reply);
        replyRepository.delete(deleteTarget);

    }


    private Reply findDeleteTarget(Reply reply) {
        Reply parent = reply.getParent();

        if (canDeleteParent(parent)) {
            return findDeleteTarget(parent);
        }
        return reply;
    }

    private static boolean canDeleteParent(Reply parent) {
        return parent != null && parent.getIsDeleted() &&  parent.getChildren().size() == 1 ;
    }


}
