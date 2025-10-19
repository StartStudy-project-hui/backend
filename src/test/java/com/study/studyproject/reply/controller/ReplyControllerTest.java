package com.study.studyproject.reply.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.study.studyproject.board.repository.BoardRepository;
import com.study.studyproject.board.domain.Board;
import com.study.studyproject.board.domain.Category;
import com.study.studyproject.member.domain.Member;
import com.study.studyproject.reply.domain.Reply;
import com.study.studyproject.global.jwt.JwtUtil;
import com.study.studyproject.login.dto.TokenDtoResponse;
import com.study.studyproject.member.repository.MemberRepository;
import com.study.studyproject.reply.dto.ReplyRequestDto;
import com.study.studyproject.reply.dto.UpdateReplyRequest;
import com.study.studyproject.reply.repository.ReplyRepository;
import com.study.studyproject.reply.service.ReplyServiceImpl;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static com.study.studyproject.board.domain.Category.CS;
import static com.study.studyproject.login.domain.Role.ROLE_USER;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@AutoConfigureMockMvc
@SpringBootTest
@Transactional
class ReplyControllerTest {
    @Autowired
    ReplyRepository replyRepository;

    @Autowired
    MemberRepository memberRepository;

    @Autowired
    BoardRepository boardRepository;

    @Autowired
    ReplyServiceImpl replyService;
    @Autowired
    JwtUtil jwtUtil;
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @AfterEach
    void tearDown() {
        replyRepository.deleteAllInBatch();
        boardRepository.deleteAllInBatch();
        memberRepository.deleteAllInBatch();
    }

    @Test
    @DisplayName("게시글에 댓글 작성하다.")
    void insertParentReply() throws Exception {
        //given
        Member member1 = createMember("jacom2@naver.com", "1234", "사용자명1", "닉네임1");
        Board boardCreate = createBoard(member1, "제목1", "내용1", "닉네임1", CS);
        memberRepository.save(member1);
        boardRepository.save(boardCreate);


        TokenDtoResponse allToken = jwtUtil.createAllToken(member1.getEmail(), member1.getId());

        ReplyRequestDto replyRequestDto = new ReplyRequestDto(boardCreate.getId(), null, "댓글내용");
        //when
        mockMvc.perform(post("/api/v1/reply")
                        .header(jwtUtil.ACCESS_TOKEN, jwtUtil.BEARER + allToken.getAccessToken())
                        .content(objectMapper.writeValueAsString(replyRequestDto))
                        .contentType(MediaType.APPLICATION_JSON)
                )
                .andDo(print())
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("게시글에 댓글 작성할 때, 게시글 번호와 댓글 내용은 필수값입니다.")
    void insertParentReplyWithoutBoardId() throws Exception {
        //given
        Member member1 = createMember("jacom2@naver.com", "1234", "사용자명1", "닉네임1");
        Board boardCreate = createBoard(member1, "제목1", "내용1", "닉네임1", CS);
        memberRepository.save(member1);
        boardRepository.save(boardCreate);

        ReplyRequestDto replyRequestDto = ReplyRequestDto.builder()
                .build();

        TokenDtoResponse allToken = jwtUtil.createAllToken(member1.getEmail(), member1.getId());

        //when
        mockMvc.perform(post("/api/v1/reply")
                        .header(jwtUtil.ACCESS_TOKEN, jwtUtil.BEARER + allToken.getAccessToken())
                        .content(objectMapper.writeValueAsString(replyRequestDto))
                        .contentType(MediaType.APPLICATION_JSON)

                )
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.boardId").value("게시글 번호를 입력해주세요."))
                .andExpect(jsonPath("$.content").value("댓글 내용을 입력해주세요"));

    }


    @Test
    @DisplayName("게시글에 대댓글 작성하다.")
    void insertChildReply() throws Exception {
        //given
        Member member1 = createMember("jacom2@naver.com", "1234", "사용자명1", "닉네임1");
        Board boardCreate = createBoard(member1, "제목1", "내용1", "닉네임1", CS);
        Reply parentReply = createReply("첫댓글",null, member1,boardCreate) ; //1

        memberRepository.save(member1);
        boardRepository.save(boardCreate);
        replyRepository.save(parentReply);

        TokenDtoResponse allToken = jwtUtil.createAllToken(member1.getEmail(), member1.getId());
        ReplyRequestDto replyRequestDto = new ReplyRequestDto(boardCreate.getId(), parentReply.getId(), "대댓글 내용");


        //when
        mockMvc.perform(post("/api/v1/reply")
                        .header(jwtUtil.ACCESS_TOKEN, jwtUtil.BEARER + allToken.getAccessToken())
                        .content(objectMapper.writeValueAsString(replyRequestDto))
                        .contentType(MediaType.APPLICATION_JSON)
                )
                .andDo(print())
                .andExpect(status().isOk());

    }
    @Test
    @DisplayName("게시글 댓글을 수정한다.")
    @WithMockUser
    void updateReply() throws Exception {

        Member member1 = createMember("jacom2@naver.com", "1234", "사용자명1", "닉네임1");
        memberRepository.save(member1);
        Board board = createBoard(member1, "제목1", "내용1", "닉네임1", CS);
        boardRepository.save(board);

        Reply one = createReply("댓글1", null, member1, board);
        replyRepository.saveAll(List.of(one));
        TokenDtoResponse allToken = jwtUtil.createAllToken(member1.getEmail(), member1.getId());
        UpdateReplyRequest replyRequestDto = UpdateReplyRequest.builder()
                .replyId(one.getId())
                .content("수정된 댓글")
                .build();



        //when
        mockMvc.perform(patch("/api/v1/reply")
                        .header(jwtUtil.ACCESS_TOKEN, jwtUtil.BEARER + allToken.getAccessToken())
                        .content(objectMapper.writeValueAsString(replyRequestDto))
                        .contentType(MediaType.APPLICATION_JSON)
                )
                .andDo(print())
                .andExpect(status().isOk());
    }


    @Test
    @DisplayName("게시글 댓글 수정에 댓글 번호와 내용은 필수값입니다.")
    @WithMockUser
    void updateReplyWithoutReplyNumAndContent() throws Exception {

        Member member1 = createMember("jacom2@naver.com", "1234", "사용자명1", "닉네임1");
        memberRepository.save(member1);
        Board board = createBoard(member1, "제목1", "내용1", "닉네임1", CS);
        boardRepository.save(board);

        Reply one = createReply("댓글1", null, member1, board);
        replyRepository.saveAll(List.of(one));
        TokenDtoResponse allToken = jwtUtil.createAllToken(member1.getEmail(), member1.getId());
        UpdateReplyRequest replyRequestDto = UpdateReplyRequest.builder()
                .build();



        //when
        mockMvc.perform(patch("/api/v1/reply")
                        .header(jwtUtil.ACCESS_TOKEN, jwtUtil.BEARER + allToken.getAccessToken())
                        .content(objectMapper.writeValueAsString(replyRequestDto))
                        .contentType(MediaType.APPLICATION_JSON)
                )
                .andDo(print())
                .andExpect(status().isBadRequest());
    }



    @Test
    @DisplayName("댓글을 삭제한다.")
    @WithMockUser
    void insertDeleteReply() throws Exception {
        //given
        Member member1 = createMember("jacom2@naver.com", "1234", "사용자명1", "닉네임1");
        memberRepository.save(member1);
        Board board = createBoard(member1, "제목1", "내용1", "닉네임1", CS);
        boardRepository.save(board);

        Reply one = createReply("댓글1", null, member1, board);
        replyRepository.save(one);
        TokenDtoResponse allToken = jwtUtil.createAllToken(member1.getEmail(), member1.getId());


        //when
        mockMvc.perform(delete("/api/v1/reply/"+one.getId())
                        .header(jwtUtil.ACCESS_TOKEN, jwtUtil.BEARER + allToken.getAccessToken())
                        .contentType(MediaType.APPLICATION_JSON)
                )
                .andDo(print())
                .andExpect(status().isOk());

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
    private Reply createReply(
            String content, Reply parent,  Member member, Board board
    ) {
        return Reply.builder()
                .member(member)
                .parent(parent)
                .content(content)
                .board(board)
                .build();
    }

}