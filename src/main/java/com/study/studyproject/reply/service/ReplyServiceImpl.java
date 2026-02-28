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
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static com.study.studyproject.global.exception.ex.ErrorCode.*;
import static com.study.studyproject.reply.dto.ReplyResponseDto.ReplyResponseToDto;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class ReplyServiceImpl implements ReplyService {


    private final BoardRepository boardRepository;
    private final ReplyRepository replyRepository;
    private final MemberRepository memberRepository;


    @Transactional(readOnly = true)
    public ReplyResponseDto getRepliesForOneBoard(Long boardId) {
        List<Reply> replies = replyRepository.findByBoardReply(boardId);
        List<ReplyInfoResponseDto> replyResponseDTOList = getReplyInfoResponseDtos(replies);
        return ReplyResponseToDto(replies.size(), replyResponseDTOList);
    }

    private  List<ReplyInfoResponseDto> getReplyInfoResponseDtos(List<Reply> replies) {


        Map<Long, ReplyInfoResponseDto> replyMap = createDtoMap(replies);

        List<ReplyInfoResponseDto> rootNodes = new ArrayList<>();

        for (Reply reply : replies) {
            ReplyInfoResponseDto currentDto = replyMap.get(reply.getId());
            if (isChild(reply)) {
                appendToBeParent(reply, replyMap, currentDto);
                continue;
            }
            //루트 노트
            rootNodes.add(currentDto);

        }

        return rootNodes;
    }


    private static void appendToBeParent(Reply reply, Map<Long, ReplyInfoResponseDto> replyMap, ReplyInfoResponseDto currentDto) {
        Long parentId = reply.getParent().getId();
        replyMap.get(parentId).getChildren().add(currentDto);
    }

    private static Map<Long, ReplyInfoResponseDto> createDtoMap(List<Reply> comments) {
        return comments.stream().map(ReplyInfoResponseDto::from)
                .collect(Collectors.toMap(ReplyInfoResponseDto::getReplyId, dto -> dto));
    }

    private  boolean isChild(Reply reply) {
        return reply.getParent() != null;
    }

    public void insert(Long memberId, ReplyRequestDto replyRequestDto) {
        Board board = findByBoardId(replyRequestDto);
        Member member = findByMemberId(memberId);
        Reply parent = getParentIfPresent(replyRequestDto);
        Reply reply = Reply.createReply(
                replyRequestDto.getContent(),
                board,
                member,
                parent
        );

        replyRepository.save(reply);
    }

    private Reply getParentIfPresent(ReplyRequestDto replyRequestDto) {
        Reply parent = null;
        if (replyRequestDto.isReplyParent()) { // 대댓글인 경우
            parent = findByReply(replyRequestDto);
        }
        return parent;
    }

    private Reply findByReply(ReplyRequestDto replyRequestDto) {
        return replyRepository.findById(replyRequestDto.getParentId())
                .orElseThrow(() -> new NotFoundException(REPLY_NOT_FOUND));
    }

    private Member findByMemberId(Long memberId) {
        return memberRepository
                .findById(memberId)
                .orElseThrow(() -> new NotFoundException(MEMBER_NOT_FOUND));
    }

    private Board findByBoardId(ReplyRequestDto replyRequestDto) {
        return boardRepository.findById(replyRequestDto.getBoardId())
                .orElseThrow(() -> new NotFoundException(BOARD_NOT_FOUND));
    }


    @Override
    public void updateReply(UpdateReplyRequest updateReplyRequest) {
        Reply findReply = replyRepository.findById(updateReplyRequest.getReplyId()).orElseThrow(() -> new NotFoundException(REPLY_NOT_FOUND));
        findReply.updateReply(updateReplyRequest.getContent());
    }


    @Override
    public void deleteReply(Long num) { //댓글 num
        Reply reply = replyRepository.findCommentByIdWithParent(num)
                .orElseThrow(() -> new NotFoundException(REPLY_NOT_FOUND));

        reply.anonymize();
    }


}
