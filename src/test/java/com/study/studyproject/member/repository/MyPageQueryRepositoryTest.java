package com.study.studyproject.member.repository;

import com.study.studyproject.board.domain.Board;
import com.study.studyproject.board.domain.Category;
import com.study.studyproject.board.repository.BoardRepository;
import com.study.studyproject.global.jwt.JwtUtil;
import com.study.studyproject.list.dto.ListResponseDto;
import com.study.studyproject.member.domain.Member;
import com.study.studyproject.member.dto.MemberListRequestDto;
import com.study.studyproject.reply.repository.ReplyRepository;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

import java.util.List;

import static com.study.studyproject.board.domain.Category.CS;
import static com.study.studyproject.board.domain.Category.기타;
import static com.study.studyproject.login.domain.Role.ROLE_USER;
import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Transactional
class MyPageQueryRepositoryTest {

    @Autowired
    MemberRepository memberRepository;

    @Autowired
    BoardRepository boardRepository;

    @Autowired
    ReplyRepository replyRepository;

    @Autowired
    MyPageQueryRepository myPageQueryRepository;

    @Autowired
    JwtUtil jwtUtil;

    @AfterEach
    void tearDown() {
        replyRepository.deleteAllInBatch();
        boardRepository.deleteAllInBatch();
        memberRepository.deleteAllInBatch();
    }

    @Test
    @DisplayName("마이페이지의 내가 작성한 글 조회한다. ")
    void myPageListPage() {
        //given
        Member member1 = createMember("jacom2@naver.com", "!12341234", "사용자명1", "닉네임1");
        memberRepository.save(member1);


        Member member2 = createMember("jacom1@naver.com", "!kimkimkim", "사용자명1", "닉네임1");
        memberRepository.save(member2);

        Board board = createBoard(member2, "제목1", "내용1", "닉네임1", CS);
        Board board1 = createBoard(member2, "제목2", "내용2", "닉네임1", CS);
        Board board2 = createBoard(member2, "제목3", "내용3", "닉네임1", 기타);

        List<Board> products = List.of(board, board1, board2);
        boardRepository.saveAll(products);

        MemberListRequestDto listRequestDto = MemberListRequestDto.builder()
                .category(CS)
                .order(0)
                .build();

        PageRequest pageRequest = PageRequest.of(0, 3);

        //when
        Page<ListResponseDto> listResponseDtos = myPageQueryRepository.MyPageListPage(listRequestDto, pageRequest, member2.getId(), "Admin");


        //then
        List<ListResponseDto> content = listResponseDtos.getContent();
        assertThat(content).
                hasSize(2);

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