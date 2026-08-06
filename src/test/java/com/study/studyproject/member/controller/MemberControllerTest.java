package com.study.studyproject.member.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.study.studyproject.board.domain.Board;
import com.study.studyproject.board.domain.Category;
import com.study.studyproject.board.repository.BoardRepository;
import com.study.studyproject.global.jwt.JwtUtil;
import com.study.studyproject.auth.dto.TokenDtoResponse;
import com.study.studyproject.member.domain.Member;
import com.study.studyproject.member.dto.MemberListRequestDto;
import com.study.studyproject.member.dto.MemberUpdateResDto;
import com.study.studyproject.member.repository.MemberRepository;
import com.study.studyproject.member.service.MemberServiceImpl;
import com.study.studyproject.postlike.domain.PostLike;
import com.study.studyproject.postlike.repository.PostLikeRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.result.MockMvcResultHandlers;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import java.util.List;

import static com.study.studyproject.board.domain.Category.*;
import static com.study.studyproject.auth.domain.Role.ROLE_USER;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

//@WebMvcTest(controllers = MemberController.class)
@AutoConfigureMockMvc
@SpringBootTest
@Transactional
class MemberControllerTest {

    @Autowired
    PasswordEncoder passwordEncoder;


    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private  MemberServiceImpl memberService;

    @Autowired
    private BoardRepository boardRepository;

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private  JwtUtil jwtUtil;

    @Autowired
    PostLikeRepository postLikeRepository;

    @Test
    @DisplayName("사용자 정보를 조회한다.")
    @WithMockUser
    void userInfo() throws Exception {
        //given
        Member member = createMember("jacom1@naver.com", passwordEncoder.encode("!112341234"), "사용자명1", "닉네임1");
        memberRepository.save(member);

        TokenDtoResponse allToken = jwtUtil.createAllToken(member.getEmail(), member.getId());

        //when         //then
        mockMvc.perform(get("/api/v1/user/info")
                        .header(jwtUtil.ACCESS_TOKEN, jwtUtil.BEARER+allToken.getAccessToken())
                        .header(jwtUtil.REFRESH_TOKEN, jwtUtil.BEARER+allToken.getRefreshToken())
                )
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value("사용자명1"))
                .andExpect(jsonPath("$.email").value("jacom1@naver.com"))
                .andExpect(jsonPath("$.nickname").value("닉네임1"));
    }


    @Test
    @DisplayName("비회원이 사용자 정보를 접근하려고 할 경우, 401에러가 발생한다.")
    void noMemneruserInfo() throws Exception {
        //given

        //when         //then
        mockMvc.perform(get("/api/v1/user/info")
                )
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isUnauthorized());
    }


    @Test
    @WithMockUser
    @DisplayName("사용자 정보 수정한다.")
    void userInfoUpdate() throws Exception {
        //given
        Member member = createMember("jacom1@naver.com", passwordEncoder.encode("!112341234"), "사용자명1", "닉네임1");
        memberRepository.save(member);
        TokenDtoResponse allToken = jwtUtil.createAllToken(member.getEmail(), member.getId());
        MemberUpdateResDto memberUpdateResDto = new MemberUpdateResDto("수정이름", "수정닉네임");

        //when
        mockMvc.perform(patch("/api/v1/user/info")
                        .header(jwtUtil.ACCESS_TOKEN, jwtUtil.BEARER + allToken.getAccessToken())
                        .header(jwtUtil.REFRESH_TOKEN, jwtUtil.BEARER + allToken.getRefreshToken())
                        .content(objectMapper.writeValueAsString(memberUpdateResDto))
                        .contentType(MediaType.APPLICATION_JSON)

                )
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("사용자 수정 성공"));
    }


    @Test
    @WithMockUser
    @DisplayName("사용자 정보 수정할 떄, 수정할 이름은 필수값이다.")
    void userInfoUpdateWithoutName() throws Exception {
        //given
        Member member = createMember("jacom1@naver.com", passwordEncoder.encode("!112341234"), "사용자명1", "닉네임1");
        memberRepository.save(member);
        TokenDtoResponse allToken = jwtUtil.createAllToken(member.getEmail(), member.getId());
        MemberUpdateResDto memberUpdateResDto = new MemberUpdateResDto(null, "수정닉네임");

        //when
        mockMvc.perform(patch("/api/v1/user/info")
                        .header(jwtUtil.ACCESS_TOKEN, jwtUtil.BEARER + allToken.getAccessToken())
                        .header(jwtUtil.REFRESH_TOKEN, jwtUtil.BEARER + allToken.getRefreshToken())
                        .content(objectMapper.writeValueAsString(memberUpdateResDto))
                        .contentType(MediaType.APPLICATION_JSON)

                )
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser
    @DisplayName("사용자 정보 수정할 떄, 수정할 이름과 닉네임은 필수값이다.")
    void userInfoUpdateWithoutNameAnd() throws Exception {
        //given
        Member member = createMember("jacom1@naver.com", passwordEncoder.encode("!112341234"), "사용자명1", "닉네임1");
        memberRepository.save(member);
        TokenDtoResponse allToken = jwtUtil.createAllToken(member.getEmail(), member.getId());
        MemberUpdateResDto memberUpdateResDto = new MemberUpdateResDto(null, null);

        //when //then
        mockMvc.perform(patch("/api/v1/user/info")
                        .header(jwtUtil.ACCESS_TOKEN, jwtUtil.BEARER + allToken.getAccessToken())
                        .header(jwtUtil.REFRESH_TOKEN, jwtUtil.BEARER + allToken.getRefreshToken())
                        .content(objectMapper.writeValueAsString(memberUpdateResDto))
                        .contentType(MediaType.APPLICATION_JSON)

                )
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("내가 작성한 모든 게시글을 최신순으로 조회한다.")
    void mainList() throws Exception {

        //given
        Member member = createMember("jacom1@naver.com", passwordEncoder.encode("!112341234"), "사용자명1", "닉네임1");
        Board board = createBoard(member, "제목1", "내용1", "닉네임1", CS);
        Board board1 = createBoard(member, "제목2", "내용2", "닉네임1", CS);
        Board board2 = createBoard(member, "제목3", "내용3", "닉네임1", ETC);
        memberRepository.save(member);
        boardRepository.saveAll(List.of(board, board1, board2));
        TokenDtoResponse allToken = jwtUtil.createAllToken(member.getEmail(), member.getId());
        MultiValueMap<String, String> query_param = new LinkedMultiValueMap<>();
        query_param.add("page","0");
        query_param.add("size","10");

        //when then
        mockMvc.perform(get("/api/v1/user/lists")
                        .header(jwtUtil.ACCESS_TOKEN, jwtUtil.BEARER + allToken.getAccessToken())
                        .header(jwtUtil.REFRESH_TOKEN, jwtUtil.BEARER + allToken.getRefreshToken())
                        .params(query_param)
                        .contentType(MediaType.APPLICATION_JSON)

                )
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("사용자가 작성한  CS카테고리 게시글을 최신순으로 조회한다.")
    void mainListWithCs() throws Exception {

        //given
        Member member = createMember("jacom1@naver.com", passwordEncoder.encode("!112341234"), "사용자명1", "닉네임1");
        Board board = createBoard(member, "제목1", "내용1", "닉네임1", CS);
        Board board1 = createBoard(member, "제목2", "내용2", "닉네임1", CS);
        Board board2 = createBoard(member, "제목3", "내용3", "닉네임1", ETC);
        memberRepository.save(member);
        boardRepository.saveAll(List.of(board, board1, board2));
        TokenDtoResponse allToken = jwtUtil.createAllToken(member.getEmail(), member.getId());
        PageRequest pageRequest = PageRequest.of(0, 10);
        MultiValueMap<String, String> query_param = new LinkedMultiValueMap<>();
        query_param.add("recruit", "모집중");
        query_param.add("category", "CS");
        query_param.add("page","0");
        query_param.add("size","10");



        //when then
        mockMvc.perform(get("/api/v1/user/lists")
                        .header(jwtUtil.ACCESS_TOKEN, jwtUtil.BEARER + allToken.getAccessToken())
                        .header(jwtUtil.REFRESH_TOKEN, jwtUtil.BEARER + allToken.getRefreshToken())
                        .params(query_param)
                )
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("모든 관심글 게시글을 최신순으로 조회한다.")
    void postLikeBoard() throws Exception {
        //given
        Member member = createMember("jacom1@naver.com", passwordEncoder.encode("!112341234"), "사용자명1", "닉네임1");
        Board board = createBoard(member, "제목1", "내용1", "닉네임1", CS);
        Board board1 = createBoard(member, "제목2", "내용2", "닉네임1", CS);
        Board board2 = createBoard(member, "제목3", "내용3", "닉네임1", ETC);
        PostLike postLike1 = PostLike.create(member, board);
        PostLike postLike2 = PostLike.create(member, board1);
        PostLike postLike3 = PostLike.create(member, board2);

        memberRepository.save(member);
        boardRepository.saveAll(List.of(board, board1, board2));
        postLikeRepository.saveAll(List.of(postLike1, postLike2, postLike3));

        MemberListRequestDto memberListRequestDto = new MemberListRequestDto();
        TokenDtoResponse allToken = jwtUtil.createAllToken(member.getEmail(), member.getId());

        int page = 0;
        int size = 10;

        //when //then
        mockMvc.perform(get("/api/v1/user/lists")
                        .header(jwtUtil.ACCESS_TOKEN, jwtUtil.BEARER + allToken.getAccessToken())
                        .param("page", String.valueOf(page))
                        .param("size", String.valueOf(size))

                )
                .andExpect(status().isOk())
                .andDo(MockMvcResultHandlers.print());

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