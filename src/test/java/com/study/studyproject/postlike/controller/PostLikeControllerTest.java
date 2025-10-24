package com.study.studyproject.postlike.controller;

import com.study.studyproject.board.repository.BoardRepository;
import com.study.studyproject.board.domain.Board;
import com.study.studyproject.board.domain.Category;
import com.study.studyproject.member.domain.Member;
import com.study.studyproject.postlike.domain.PostLike;
import com.study.studyproject.global.jwt.JwtUtil;
import com.study.studyproject.login.dto.TokenDtoResponse;
import com.study.studyproject.member.repository.MemberRepository;
import com.study.studyproject.postlike.repository.PostLikeRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.result.MockMvcResultHandlers;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static com.study.studyproject.board.domain.Category.CS;
import static com.study.studyproject.login.domain.Role.ROLE_USER;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@AutoConfigureMockMvc
@SpringBootTest
@Transactional
class PostLikeControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private BoardRepository boardRepository;

    @Autowired
    private PostLikeRepository postLikeRepository;

    @Autowired
    private  JwtUtil jwtUtil;

    @AfterEach
    void tearDown() {
        postLikeRepository.deleteAllInBatch();
        boardRepository.deleteAllInBatch();
        memberRepository.deleteAllInBatch();

    }

    @Test
    @DisplayName("관심글을 등록합니다.")
    void postLikeSave() throws Exception {
        //given
        Member member = createMember("jacom1@naver.com", "!112341234", "사용자명1", "닉네임1");
        Board board = createBoard(member, "제목1", "내용1", "닉네임1", CS);
        memberRepository.save(member);
        boardRepository.save(board);
        TokenDtoResponse allToken = jwtUtil.createAllToken(member.getEmail(), member.getId());

        System.out.println("board.getId() = " + board.getId());

        //when then
        mockMvc.perform(post("/api/v1/post-like/" +board.getId())
                        .header(jwtUtil.ACCESS_TOKEN, jwtUtil.BEARER + allToken.getAccessToken())
                        .header(jwtUtil.REFRESH_TOKEN, jwtUtil.BEARER + allToken.getRefreshToken())
                )
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isOk());
    }
    @Test
    @DisplayName("관심글을 삭제합니다.")
    void postLikeDelete() throws Exception {
        //given
        Member member = createMember("jacom1@naver.com", "!112341234", "사용자명1", "닉네임1");
        Board board = createBoard(member, "제목1", "내용1", "닉네임1", CS);
        memberRepository.save(member);
        boardRepository.saveAll(List.of(board));
        TokenDtoResponse allToken = jwtUtil.createAllToken(member.getEmail(), member.getId());
        PostLike postLike1 = PostLike.create(member, board);
        postLikeRepository.save(postLike1);


        //when then
        mockMvc.perform(delete("/api/v1/post-like/"+postLike1.getId())
                        .header(jwtUtil.ACCESS_TOKEN, jwtUtil.BEARER + allToken.getAccessToken())
                        .header(jwtUtil.REFRESH_TOKEN, jwtUtil.BEARER + allToken.getRefreshToken())
                )
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("관심글이 삭제되었습니다."))
                .andExpect(jsonPath("$.statusCode").value("200"));
        //

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