package com.study.studyproject.member.controller;

import com.study.studyproject.board.repository.BoardRepository;
import com.study.studyproject.board.domain.Board;
import com.study.studyproject.board.domain.Category;
import com.study.studyproject.member.domain.Member;
import com.study.studyproject.auth.domain.Role;
import com.study.studyproject.global.jwt.JwtUtil;
import com.study.studyproject.auth.dto.TokenDtoResponse;
import com.study.studyproject.member.repository.MemberRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import java.util.List;

import static com.study.studyproject.board.domain.Category.*;
import static com.study.studyproject.auth.domain.Role.ROLE_USER;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@AutoConfigureMockMvc
@SpringBootTest
@Transactional
class AdminControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    PasswordEncoder passwordEncoder;
    @Autowired
    private BoardRepository boardRepository;

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private JwtUtil jwtUtil;
    @Test
    @DisplayName("사용자 검색을 입력하지 않을 경우, 사용자 전체 정보 가져온다.")
    void userAllAdmin() throws Exception {
        //given
        Member adminOne = createAdmin();
        Member member1 = createMember("jacom2@naver.com", "!12341234", "사용자명1", "닉네임0");
        Member member2 = createMember("jacom1@naver.com", "!kimkimkim", "사용자명2", "닉네임1");
        Member member3 = createMember("jacom3@naver.com", "!123", "사용자명3", "닉네임2");
        Member member4 = createMember("jacom4@naver.com", "!1234", "사용자명4", "닉네임3");
        memberRepository.saveAll(List.of(adminOne,member1,member2,member3,member4));
        TokenDtoResponse allToken = jwtUtil.createAllToken(adminOne.getEmail(), adminOne.getId());
        MultiValueMap<String, String> query_param = new LinkedMultiValueMap<>();
        query_param.add("page","0");
        query_param.add("size","10");


        //when
        //when
        mockMvc.perform(get("/api/v1/admin/user-all")
                        .header(jwtUtil.ACCESS_TOKEN, jwtUtil.BEARER + allToken.getAccessToken())
                        .header(jwtUtil.REFRESH_TOKEN, jwtUtil.BEARER + allToken.getRefreshToken())
                        .params(query_param)
                )
                .andDo(print())
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("사용자 검색을 입력하면, 관련 사용자들의 정보 가져온다.")
    void userAllAdminWithTarget() throws Exception {
        //given
        Member adminOne = createAdmin();
        Member member1 = createMember("jacom2@naver.com", "!12341234", "사용자명1", "닉네임0");
        Member member2 = createMember("jacom1@naver.com", "!kimkimkim", "사용자명2", "닉네임1");
        Member member3 = createMember("jacom3@naver.com", "!123", "사용자명3", "닉네임2");
        Member member4 = createMember("jacom4@naver.com", "!1234", "사용자명4", "닉네임3");
        Member member5 = createMember("jacom5@naver.com", "!1234", "김사랑", "닉네임4");
        Member member6 = createMember("jacom6@naver.com", "!1234", "김진수", "닉네임5");
        memberRepository.saveAll(List.of(adminOne,member1,member2,member3,member4,member5,member6));
        TokenDtoResponse allToken = jwtUtil.createAllToken(adminOne.getEmail(), adminOne.getId());

        MultiValueMap<String, String> query_param = new LinkedMultiValueMap<>();
        query_param.add("page","0");
        query_param.add("username","사용자명");
        query_param.add("size","10");
        int expectedSize = 4;


        //when
        mockMvc.perform(get("/api/v1/admin/user-all")
                        .header(jwtUtil.ACCESS_TOKEN, jwtUtil.BEARER + allToken.getAccessToken())
                        .header(jwtUtil.REFRESH_TOKEN, jwtUtil.BEARER + allToken.getRefreshToken())
                        .params(query_param)
                )
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content",hasSize(4)));

    }

    @Test
    @DisplayName("관리자 정보와 모든 게시글페이지를 최신순으로 조회한다.")
    void adminDashAllBoard() throws Exception {
        //given
        Member adminOne = createAdmin();
        Member member1 = createMember("jacom2@naver.com", "!12341234", "사용자명1", "닉네임0");
        Board board = createBoard(member1, "제목1", "내용1", "닉네임1", CS);
        Board board1 = createBoard(member1, "제목2", "내용2", "닉네임2", CS);
        Board board2 = createBoard(member1, "제목3", "내용3", "닉네임3", ETC);

        memberRepository.saveAll(List.of(adminOne,member1));
        boardRepository.save(board);
        boardRepository.save(board1);
        boardRepository.save(board2);
        TokenDtoResponse allToken = jwtUtil.createAllToken(adminOne.getEmail(), adminOne.getId());

        MultiValueMap<String, String> query_param = new LinkedMultiValueMap<>();
        query_param.add("page","0");
        query_param.add("size","3");
        int expectedSize = 3;


        //when
        mockMvc.perform(get("/api/v1/admin/dash-board")
                        .header(jwtUtil.ACCESS_TOKEN, jwtUtil.BEARER + allToken.getAccessToken())
                        .params(query_param)
                )
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("김관리"))
                .andExpect(jsonPath("$.nickname").value("admin"))
                .andExpect(jsonPath("$.role").value("ROLE_ADMIN"))
                .andExpect(jsonPath("$.listResponseDto.content", hasSize(expectedSize)))
                .andExpect(jsonPath("$.listResponseDto.content").isArray());

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

    private static Member createAdmin() {
        return Member.builder()
                .role(Role.ROLE_ADMIN)
                .username("김관리")
                .email("admin@naver.com")
                .nickname("admin")
                .password("1234")
                .build();
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