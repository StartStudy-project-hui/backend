package com.study.studyproject.reply.service;

import com.study.studyproject.board.repository.BoardRepository;
import com.study.studyproject.board.domain.Board;
import com.study.studyproject.board.domain.Category;
import com.study.studyproject.member.domain.Member;
import com.study.studyproject.reply.domain.Reply;
import com.study.studyproject.global.exception.ex.NotFoundException;
import com.study.studyproject.member.repository.MemberRepository;
import com.study.studyproject.reply.dto.ReplyRequestDto;
import com.study.studyproject.reply.dto.UpdateReplyRequest;
import com.study.studyproject.reply.repository.ReplyRepository;
import jakarta.persistence.EntityManager;
import jakarta.transaction.Transactional;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;

import static com.study.studyproject.board.domain.Category.CS;
import static com.study.studyproject.login.domain.Role.ROLE_USER;
import static com.study.studyproject.global.exception.ex.ErrorCode.NOT_FOUND_REPLY;
import static org.assertj.core.api.Assertions.*;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@Transactional
@ActiveProfiles("test")
class ReplyServiceImplTest {

    @Autowired
    ReplyRepository replyRepository;

    @Autowired
    MemberRepository memberRepository;

    @Autowired
    BoardRepository boardRepository;

    @Autowired
    ReplyServiceImpl replyService;

    @Autowired
    private EntityManager entityManager;


    @AfterEach
    void tearDown() {
        replyRepository.deleteAllInBatch();
        boardRepository.deleteAllInBatch();
        memberRepository.deleteAllInBatch();
    }

    @Test
    @DisplayName("게시글에 댓글 작성하다")
    void insertReply() {
        Member member1 = createMember("jacom2@naver.com", "1234", "사용자명1", "닉네임1");
        Board boardCreate = createBoard(member1, "제목1", "내용1", "닉네임1", CS);
        memberRepository.save(member1);
        boardRepository.save(boardCreate);
        ReplyRequestDto replyOne = new ReplyRequestDto(boardCreate.getId(), null, "댓글 내용");
        //when
        replyService.insert(member1.getId(), replyOne);

        //then
        List<Reply> all = replyRepository.findAll();
        Assertions.assertThat(all).hasSize(1);


    }


    @Test
    @DisplayName("사용자가 게시글에 대댓글 작성하다")
    void insertDuplReply() {
        Member member1 = createMember("jacom2@naver.com", "1234", "사용자명1", "닉네임1");
        Board board = createBoard(member1, "제목1", "내용1",    "닉네임1", CS);
        Reply parentReply = createReply("첫댓글",board, member1,null) ; //1

        System.out.println("member1 = " + member1.getNickname());
        memberRepository.save(member1);
        boardRepository.save(board);

        replyRepository.save(parentReply);
        entityManager.flush();
        entityManager.clear();

        ReplyRequestDto replyOne = new ReplyRequestDto(board.getId(), parentReply.getId(), "댓글 내용");

        //when
        replyService.insert(member1.getId(), replyOne);

        //then
        List<Reply> all = replyRepository.findAll();
        Assertions.assertThat(all).hasSize(2);

    }




    @Test
    @DisplayName("게시글 댓글을 수정한다.")
    void updateReply() {

        Member member1 = createMember("jacom2@naver.com", "1234", "사용자명1", "닉네임1");
        memberRepository.save(member1);
        Board board = createBoard(member1, "제목1", "내용1", "닉네임1", CS);
        boardRepository.save(board);

        Reply one = createReply("댓글1", board, member1, null);
        replyRepository.saveAll(List.of(one));

        UpdateReplyRequest updateReplyRequest = new UpdateReplyRequest(one.getId(), "수정된 내용1");

        //when
        replyService.updateReply(updateReplyRequest);

        //then
        Reply reply = replyRepository.findById(one.getId()).get();
        assertThat(reply).isEqualTo(one);
        assertThat(reply.getContent()).isEqualTo(updateReplyRequest.getContent());

    }

    @Test
    @DisplayName("사용자가 대댓글이 있는 댓글을 삭제할 때, 삭제되지 않고 상태만 변한다. ")
    void deleteParentReply() {

        Member member1 = createMember("jacom2@naver.com", "1234", "사용자명1", "닉네임1");
        memberRepository.save(member1);
        Board board = createBoard(member1, "제목1", "내용1", "닉네임1", CS);
        boardRepository.save(board);

        Reply one = createReply("댓글1",board,  member1,  null);
        Reply two = createReply("대댓글1",  board,member1,one);
        Reply tree = createReply("대댓글2",  board, member1,one);
        Reply four = createReply("대댓글3",  board, member1,one);


        two.assignParent(one);
        tree.assignParent(one);
        four.assignParent(one);

        replyRepository.saveAll(List.of(one, two, tree, four));


        //when
        replyService.deleteReply(one.getId());

        //then
        Reply reply = replyRepository.findById(one.getId()).get();
        assertThat(reply.getIsDeleted()).isTrue();

    }


    @Test
    @DisplayName("사용자가 대댓글을 삭제하면 삭제된다. ")
    void deleteChildReply() {

        Member member1 = createMember("jacom2@naver.com", "1234", "사용자명1", "닉네임1");
        memberRepository.save(member1);
        Board board = createBoard(member1, "제목1", "내용1", "닉네임1", CS);
        boardRepository.save(board);

        Reply one = createReply("댓글1",board,  member1,  null);
        Reply two = createReply("대댓글1",  board,member1,one);
        Reply tree = createReply("대댓글2",  board, member1,one);
        Reply four = createReply("대댓글3",  board, member1,one);


        two.assignParent(one);
        tree.assignParent(one);
        four.assignParent(one);
        replyRepository.saveAll(List.of(one, two, tree, four));


        //when
        replyService.deleteReply(two.getId());

        //then
        List<Reply> all = replyRepository.findAll();
        Assertions.assertThat(all.get(1).getIsDeleted()).isEqualTo(true);
    }


    @Test
    @DisplayName("댓글 삭제를 하면 논리적 삭제가 진행된다. ")
    void deleteParentChildReply() {

        Member member1 = createMember("jacom2@naver.com", "1234", "사용자명1", "닉네임1");
        memberRepository.save(member1);
        Board board = createBoard(member1, "제목1", "내용1", "닉네임1", CS);
        boardRepository.save(board);

        Reply one = createReply("댓글1", board, member1,null);
        Reply two = createReply("대댓글1",board,  member1, one);
        Reply tree = createReply("대댓글2", board, member1, one);
        Reply four = createReply("대댓글3", board, member1 ,one);


        replyRepository.save(one);
        replyRepository.save(two);
        replyRepository.save(tree);
        replyRepository.save(four);
        entityManager.flush();
        entityManager.clear();


        //when
        replyService.deleteReply(one.getId());
        replyService.deleteReply(two.getId());
        replyService.deleteReply(tree.getId());
        replyService.deleteReply(four.getId());

        //then
        List<Reply> all = replyRepository.findAll();
        assertThat(all)
                .extracting("content", "isDeleted")
                .containsExactlyInAnyOrder(
                        tuple("삭제된 댓글입니다.", true),
                        tuple("삭제된 댓글입니다.", true),
                        tuple("삭제된 댓글입니다.", true),
                        tuple("삭제된 댓글입니다.", true)

                );

    }

    public  Reply createReply(String content, Board board, Member member, Reply parent) {
        Reply reply = Reply.builder()
                .content(content)
                .nickname(member.getNickname())
                .build();

        reply.assignBoard(board);
        reply.assignWriter(member);

        if (parent != null) {
            reply.assignParent(parent);
        }

        return reply;
    }

    private Member createMember
            (String email, String password, String username, String nickname) {
        {
            return Member.builder()
                    .nickname(nickname)
                    .username(username)
                    .email(email)
                    .password(password)
                    .role(ROLE_USER).build();
        }

    }



    private Board createBoard(
            Member member, String title, String content, String nickname, Category category
    ) {
        return Board.builder()
                .member(member)
                .title(title)
                .content("내용")
                .category(category)
                .build();
    }
}