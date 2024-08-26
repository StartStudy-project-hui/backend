package com.study.studyproject.list.service;

import com.study.studyproject.board.repository.BoardRepository;
import com.study.studyproject.domain.Board;
import com.study.studyproject.domain.Category;
import com.study.studyproject.domain.Member;
import com.study.studyproject.list.dto.ListResponseDto;
import com.study.studyproject.list.dto.MainRequest;
import com.study.studyproject.member.repository.MemberRepository;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

import java.util.List;

import static com.study.studyproject.domain.Category.*;
import static com.study.studyproject.domain.Role.ROLE_USER;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;

@SpringBootTest
@Transactional
class ListServiceTest {

    @Autowired
    ListService listService;
    @Autowired
    BoardRepository boardRepository;

    @Autowired
    MemberRepository memberRepository;
    @Test
    @DisplayName("타이틀 검색을 입력하지 않고 카테코리가 CS인 게시글을 최신순으로 조회한다.")
    void mainListService() throws Exception {
        //given
        Member member1 = createMember("jacom2@naver.com", "1234", "사용자명1", "닉네임1");
        memberRepository.save(member1);

        Board board = createBoard(member1, "제목1", "내용1", "닉네임1", CS);
        Board board1 = createBoard(member1, "제목2", "내용2", "닉네임2", CS);
        Board board2 = createBoard(member1, "자바공부 풀 사람1", "내용3", "닉네임3", 기타);
        Board board4 = createBoard(member1, "타이틀1", "내용3", "닉네임3", CS);

         boardRepository.save(board);
        Thread.sleep(1000);
        boardRepository.save(board1);
        Thread.sleep(1000);
        boardRepository.save(board2);
        Thread.sleep(1000);
        boardRepository.save(board4);

        MainRequest mainRequest = new MainRequest(CS, 0);
        PageRequest pageRequest = PageRequest.of(0, 3);

        //when
        Page<ListResponseDto> list = listService.list(null, mainRequest, pageRequest);

        //then
        List<ListResponseDto> boardList = list.getContent();
        assertThat(boardList).hasSize(3)
                .extracting("title","content", "nickname","type")
                .containsExactly(
                        tuple("타이틀1", "내용3","닉네임3","CS"),
                        tuple("제목2", "내용2","닉네임2","CS"),
                        tuple("제목1", "내용1","닉네임1","CS")
                );

    }



    @Test
    @DisplayName("타이틀 검색을 입력하고 CS를 최신순으로 게시글을 조회한다.")
    void getTitlemainListService() throws Exception {
        //given
        Member member1 = createMember("jacom2@naver.com", "1234", "사용자명1", "닉네임1");
        memberRepository.save(member1);

        Board board = createBoard(member1, "제목1", "내용1", "닉네임1", CS);
        Board board1 = createBoard(member1, "제목2", "내용2", "닉네임2", CS);
        Board board2 = createBoard(member1, "자바공부 풀 사람1", "내용3", "닉네임3", 기타);
        Board board3 = createBoard(member1, "제목15", "내용3", "닉네임3", 코테);
        Board board4 = createBoard(member1, "타이틀1", "내용3", "닉네임3", CS);
        List<Board> products = List.of( board2,board3,board4);

        boardRepository.save(board);
        Thread.sleep(1000);

        boardRepository.save(board1);
        Thread.sleep(1000);

        List<Board> boards = boardRepository.saveAll(products);

        MainRequest mainRequest = new MainRequest(CS, 0);
        PageRequest pageRequest = PageRequest.of(0, 3);

        String title = "제목";

        //when
        Page<ListResponseDto> list = listService.list(title, mainRequest, pageRequest);

        //then
        List<ListResponseDto> boardList = list.getContent();
        assertThat(boardList).hasSize(2)
                .extracting("title","content", "nickname","type")
                .containsExactly(
                        tuple("제목2", "내용2","닉네임2","CS"),
                        tuple("제목1", "내용1","닉네임1","CS")
                );

    }


    @Test
    @DisplayName("타이틀 검색을 입력하고 조회수순으로 게시글을 조회한다.")
    void getTitleViewCntMainListService() throws Exception {
        //given
        Member member1 = createMember("jacom2@naver.com", "1234", "사용자명1", "닉네임1");
        memberRepository.save(member1);

        Board board0 = createBoard(member1, "제목1", "내용1", "닉네임1", CS);
        Board board1 = createBoard(member1, "제목2", "내용2", "닉네임2", CS);
        Board board2 = createBoard(member1, "자바공부 풀 사람1", "내용3", "닉네임3", 기타);
        Board board3 = createBoard(member1, "제목15", "내용3", "닉네임3", 코테);
        Board board4 = createBoard(member1, "타이틀1", "내용3", "닉네임3", 코테);

        //조회수 증가
        board0.updateViewCnt();


        List<Board> products = List.of(board0, board1, board2,board3,board4);
        List<Board> boards = boardRepository.saveAll(products);

        MainRequest mainRequest = new MainRequest(null,1);
        PageRequest pageRequest = PageRequest.of(0, 3);
        String title = "제목";

        //when
        Page<ListResponseDto> list = listService.list(title, mainRequest, pageRequest);

        //then
        List<ListResponseDto> boardList = list.getContent();
        assertThat(boardList).hasSize(3)
                .extracting("title","content", "nickname","type")
                .containsExactly(
                        tuple("제목1", "내용1","닉네임1","CS"),
                        tuple("제목2", "내용2","닉네임2","CS"),
                        tuple("제목15", "내용3","닉네임3","코테")
                );

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
                .content(content)
                .nickname(nickname)
                .category(category)
                .build();
    }




}