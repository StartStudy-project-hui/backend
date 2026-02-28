package com.study.studyproject.list.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.study.studyproject.board.domain.ConnectionType;
import com.study.studyproject.board.domain.OfflineLocation;
import com.study.studyproject.board.repository.BoardRepository;
import com.study.studyproject.board.domain.Board;
import com.study.studyproject.board.domain.Category;
import com.study.studyproject.list.dto.MainRequestDto;
import com.study.studyproject.member.domain.Member;
import com.study.studyproject.member.repository.MemberRepository;
import com.study.studyproject.reply.dto.ReplyRequestDto;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

import static com.study.studyproject.board.domain.Category.*;
import static com.study.studyproject.board.domain.Category.코테;
import static com.study.studyproject.board.domain.ConnectionType.OFFLINE;
import static com.study.studyproject.board.domain.ConnectionType.ONLINE;
import static com.study.studyproject.login.domain.Role.ROLE_USER;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@AutoConfigureMockMvc
@SpringBootTest
@Transactional
class MainControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private BoardRepository boardRepository;

    @Autowired
    private MemberRepository memberRepository;
    @Autowired
    private ObjectMapper objectMapper;

    @AfterEach
    void tearDown() {
        boardRepository.deleteAllInBatch();
        memberRepository.deleteAllInBatch();
    }

    @Test
    @DisplayName("모든 카테고리를 최신순으로 조회한다.")
    void mainListAllTestCreateDateDesc() throws Exception {
        //given
        Member member1 = createMember("jacom2@naver.com", "!12341234", "사용자명1", "닉네임0");
        Board board = createBoard(member1, "제목1", "내용1", "닉네임1", CS,ONLINE);
        Board board1 = createBoard(member1, "제목2", "내용2", "닉네임2", CS,ONLINE);
        Board board2 = createBoard(member1, "제목3", "내용3", "닉네임3", 기타,ONLINE);

        memberRepository.save(member1);
        boardRepository.save(board);
        boardRepository.save(board1);
        boardRepository.save(board2);

        //when & then
        mockMvc.perform(get("/api/v1/")
                )
                .andDo(print())
                .andExpect(status().isOk());
    }


    @Test
    @DisplayName("모든 카테고리를 조회순 조회한다.")
    void mainListAllTestAsc() throws Exception {
        //given
        Member member1 = createMember("jacom2@naver.com", "!12341234", "사용자명1", "닉네임0");
        Board board = createBoard(member1, "제목1", "내용1", "닉네임1", CS,ONLINE);
        Board board1 = createBoard(member1, "제목2", "내용2", "닉네임2", CS,ONLINE);
        Board board2 = createBoard(member1, "제목3", "내용3", "닉네임3", 기타,OFFLINE);

        memberRepository.saveAll(List.of(member1));
        boardRepository.save(board);
        boardRepository.save(board1);
        boardRepository.save(board2);

        board1.updateViewCnt();

        //when & then
        mockMvc.perform(get("/api/v1/")
                )
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.pageable.pageSize").value("10"))
                .andExpect(jsonPath("$.totalElements").value("3"));

    }


    @Test
    @DisplayName("title을 검색하여 관련된 게시글만 조회한다.")
    void mainListAlltarget() throws Exception {
        //given
        Member member1 = createMember("jacom2@naver.com", "!12341234", "사용자명1", "닉네임0");
        Board board0 = createBoard(member1, "제목1", "내용1", "닉네임1", CS, ONLINE);
        Board board1 = createBoard(member1, "제목2", "내용2", "닉네임2", CS,ONLINE);
        Board board2 = createBoard(member1, "자바공부 풀 사람1", "내용3", "닉네임3", 기타,ONLINE);
        Board board3 = createBoard(member1, "제목15", "내용3", "닉네임3", 코테,ONLINE);
        Board board4 = createBoard(member1, "타이틀1", "내용3", "닉네임3", 코테,ONLINE);

        memberRepository.save((member1));
        List<Board> products = List.of(board0, board1, board2,board3,board4);
        List<Board> boards = boardRepository.saveAll(products);

        board1.updateViewCnt();

        String title = "제목";


        //when & then
        mockMvc.perform(get("/api/v1/")
                        .param("title", title)

                )
                .andDo(print())
                .andExpect(status().isOk());

    }


    @Test
    @DisplayName("오프라인과  CS 카테고리의 게시글만 최신순으로 조회한다.")
    void mainListAllWithCS() throws Exception {
        //given
        Member member1 = createMember("jacom2@naver.com", "!12341234", "사용자명1", "닉네임0");
        Board board0 = createBoard(member1, "제목1", "내용1", "닉네임1", CS,ONLINE);
        Board board1 = createBoard(member1, "제목2", "내용2", "닉네임2", CS,ONLINE);
        Board board2 = createBoard(member1, "자바공부 풀 사람1", "내용3", "닉네임3", 기타,OFFLINE);
        Board board3 = createBoard(member1, "제목15", "내용3", "닉네임3", CS,OFFLINE);
        Board board4 = createBoard(member1, "타이틀1", "내용3", "닉네임3", 코테,OFFLINE);

        memberRepository.save((member1));
        boardRepository.save(board0);
        boardRepository.save(board1);
        List<Board> products = List.of( board2,board3,board4);

        List<Board> boards = boardRepository.saveAll(products);


        //when & then
        mockMvc.perform(get("/api/v1/")
                        .param("Category", "CS")
                        .param("connectionType", "OFFLINE")

                )
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].title").value("제목15"))
                .andExpect(jsonPath("$.content[0].type").value("CS"))
                .andExpect(jsonPath("$.totalPages").value("1"));


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
            Member member, String title, String content, String nickname, Category category, ConnectionType connectionType
    ) {
        return Board.builder()
                .member(member)
                .title(title)
                .content("내용")
                .category(category)
                .connectionType(connectionType)
                .build();
    }



}