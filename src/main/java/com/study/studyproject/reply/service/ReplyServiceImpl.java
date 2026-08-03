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

    @Override
    @Transactional(readOnly = true)
    public ReplyResponseDto getRepliesForOneBoard(Long boardId) {
        List<Reply> replies = replyRepository.findByBoardReply(boardId);
        List<ReplyInfoResponseDto> replyResponseDtoList = getReplyInfoResponseDtos(replies);
        return ReplyResponseToDto(replies.size(), replyResponseDtoList);
    }

    private static List<ReplyInfoResponseDto> getReplyInfoResponseDtos(List<Reply> replies) {
        Map<Long, ReplyInfoResponseDto> replyDtoMap = toReplyDtoMap(replies);
        return linkParentChild(replies, replyDtoMap);
    }

    // 모든 댓글을 먼저 DTO로 변환해 맵에 등록 (자식이 부모보다 먼저 처리되어도 안전하도록)
    private static Map<Long, ReplyInfoResponseDto> toReplyDtoMap(List<Reply> replies) {
        Map<Long, ReplyInfoResponseDto> replyDtoMap = new HashMap<>();
        for (Reply reply : replies) {
            replyDtoMap.put(reply.getId(), convertReplyToDto(reply));
        }
        return replyDtoMap;
    }

    // 부모-자식 관계 연결. 부모가 목록에 없는 경우(데이터 불일치)에는 최상위로 취급
    private static List<ReplyInfoResponseDto> linkParentChild(List<Reply> replies, Map<Long, ReplyInfoResponseDto> replyDtoMap) {
        List<ReplyInfoResponseDto> replyResponseDtoList = new ArrayList<>();

        for (Reply reply : replies) {
            ReplyInfoResponseDto replyResponseDto = replyDtoMap.get(reply.getId());
            ReplyInfoResponseDto parentDto = reply.getParent() != null
                    ? replyDtoMap.get(reply.getParent().getId())
                    : null;

            if (parentDto != null) {
                parentDto.getChildren().add(replyResponseDto);
            } else {
                replyResponseDtoList.add(replyResponseDto);
            }
        }

        return replyResponseDtoList;
    }


    @Override
    public void insert(Long memberId, ReplyRequestDto replyRequestDto) {
        Board board = findBoard(replyRequestDto.getBoardId());
        Member member = findMember(memberId);
        Reply reply = Reply.toEntity(replyRequestDto, board, member);

        if (replyRequestDto.isReplyParent()) { // 대댓글인 경우
            Reply replyParent = findParentReply(replyRequestDto.getParentId());
            reply.updateParent(replyParent);
        }

        reply.updateBoard(board);
        reply.updateWriter(member);

        replyRepository.save(reply);
    }

    private Reply findParentReply(Long parentId) {
        return replyRepository.findById(parentId)
                .orElseThrow(() -> new NotFoundException(NOT_FOUND_REPLY));
    }

    private Member findMember(Long memberId) {
        return memberRepository.findById(memberId)
                .orElseThrow(() -> new NotFoundException(NOT_FOUND_MEMBER));
    }

    private Board findBoard(Long boardId) {
        return boardRepository.findById(boardId)
                .orElseThrow(() -> new NotFoundException(NOT_FOUND_BOARD));
    }


    @Override
    public void updateReply(UpdateReplyRequest updateReplyRequest) {
        Reply findReply = replyRepository.findById(updateReplyRequest.getReplyId())
                .orElseThrow(() -> new NotFoundException(NOT_FOUND_REPLY));
        findReply.updateReply(updateReplyRequest.getContent());
    }


    @Override
    public void deleteReply(Long replyId) {
        Reply reply = replyRepository.findCommentByIdWithParent(replyId)
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
