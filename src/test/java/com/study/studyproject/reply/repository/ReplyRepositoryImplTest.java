package com.study.studyproject.reply.repository;

import com.study.studyproject.board.repository.BoardRepository;
import com.study.studyproject.board.domain.Board;
import com.study.studyproject.board.domain.Category;
import com.study.studyproject.member.domain.Member;
import com.study.studyproject.reply.domain.Reply;
import com.study.studyproject.member.repository.MemberRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static com.study.studyproject.board.domain.Category.CS;
import static com.study.studyproject.login.domain.Role.ROLE_USER;
import static org.assertj.core.api.Assertions.*;

@SpringBootTest
@Transactional
class ReplyRepositoryImplTest {

    @Autowired
    ReplyRepository replyRepository;

    @Autowired
    MemberRepository memberRepository;

    @Autowired
    BoardRepository boardRepository;

    @AfterEach
    void tearDown() {
        replyRepository.deleteAllInBatch();
        boardRepository.deleteAllInBatch();
        memberRepository.deleteAllInBatch();

    }

    @Test
    @DisplayName("해당 게시글의 댓글과 부모 댓글와 작성자를 함께 조회한다.")
    void findByBoardReply() {
        Member member1 = createMember("jacom2@naver.com", "1234", "사용자명1", "닉네임1");
        memberRepository.save(member1);
        Board board = createBoard(member1, "제목1", "내용1", "닉네임1", CS);
        boardRepository.save(board);

        Reply one = createReply("댓글1", null, member1, board);
        Reply two = createReply("대댓글1", one, member1, board);
        Reply tree = createReply("대댓글2", one, member1, board);
        Reply four = createReply("대댓글3", one, member1, board);

        Reply one2 = createReply("댓글2", null, member1, board);
        Reply two2 = createReply("대댓글2-1", one, member1, board);
        Reply tree2 = createReply("대댓글2-2", one, member1, board);
        Reply four2 = createReply("대댓글2-3", one, member1, board);
        replyRepository.saveAll(List.of(one, two, tree, four,one2,two2,tree2,four2));


        List<Reply> byBoardReply = replyRepository.findByBoardReply(board.getId());


        assertThat(byBoardReply.get(0)).isEqualTo(one);
        assertThat(byBoardReply.get(1)).isEqualTo(one2);
        assertThat(byBoardReply.get(2)).isEqualTo(two);
        assertThat(byBoardReply.get(3)).isEqualTo(tree);


    }

    @Test
    @DisplayName("게시글의 댓글들을 조회한다.")
    void findByBoardReplies() throws Exception {
        //given
        Member member1 = createMember("jacom2@naver.com", "1234", "사용자명1", "닉네임1");
        memberRepository.save(member1);
        Board board = createBoard(member1, "제목1", "내용1", "닉네임1", CS);
        boardRepository.save(board);

        Reply one = createReply("댓글1", null, member1, board);
        Reply two = createReply("대댓글1", one, member1, board);
        Reply tree = createReply("대댓글2", one, member1, board);
        Reply four = createReply("대댓글3", one, member1, board);
        replyRepository.saveAll(List.of(one, two, tree, four));

        //when
        List<Reply> byBoardReply = replyRepository.findByBoardReplies(board.getId());
        //then

        assertThat(byBoardReply.get(0)).isEqualTo(one);
        assertThat(byBoardReply.get(1)).isEqualTo(two);
        assertThat(byBoardReply.get(2)).isEqualTo(tree);
        assertThat(byBoardReply.get(3)).isEqualTo(four);
    }

    @Test
    @DisplayName("부모댓글과 대댓글을 함께 조회할 경우, 부모댓글이 없다면 null이 반환된다.")
    void getParentFindCommentByIdWithParent() throws Exception {
        //given
        Member member1 = createMember("jacom2@naver.com", "1234", "사용자명1", "닉네임1");
        memberRepository.save(member1);
        Board board = createBoard(member1, "제목1", "내용1", "닉네임1", CS);
        boardRepository.save(board);

        Reply one = createReply("댓글1", null, member1, board);
        Reply two = createReply("대댓글1", one, member1, board);
        Reply tree = createReply("대댓글2", one, member1, board);
        Reply four = createReply("대댓글3", one, member1, board);
        replyRepository.saveAll(List.of(one, two, tree, four));

        //when
        Reply reply = replyRepository.findCommentByIdWithParent(one.getId()).get();

        //then
        assertThat(reply).isEqualTo(one);
        assertThat(reply.getParent()).isNull();
    }


    @Test
    @DisplayName("댓글과 해당 댓글의 부모 댓글도 함께 조회한다.")
    void findCommentByIdWithParent2() throws Exception {
        //given
        Member member1 = createMember("jacom2@naver.com", "1234", "사용자명1", "닉네임1");
        memberRepository.save(member1);
        Board board = createBoard(member1, "제목1", "내용1", "닉네임1", CS);
        boardRepository.save(board);

        Reply one = createReply("댓글1", null, member1, board);
        Reply two = createReply("대댓글1", one, member1, board);
        Reply tree = createReply("대댓글2", one, member1, board);
        Reply four = createReply("대댓글3", one, member1, board);
        replyRepository.saveAll(List.of(one, two, tree, four));

        //when
        Reply reply = replyRepository.findCommentByIdWithParent(two.getId()).get();

        //then
        assertThat(reply).isEqualTo(two);
        assertThat(reply.getParent()).isEqualTo(one);
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

    private Reply createReply(
            String content, Reply parent,  Member member, Board board
    ) {
        return Reply.builder()
                .member(member)
                .parent(parent)
                .content("내용")
                .board(board)
                .build();
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
}
