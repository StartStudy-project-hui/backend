package com.study.studyproject.member.service;

import com.study.studyproject.board.domain.Board;
import com.study.studyproject.board.domain.Category;
import com.study.studyproject.board.domain.Recruit;
import com.study.studyproject.board.repository.BoardRepository;
import com.study.studyproject.list.dto.ListResponseDto;
import com.study.studyproject.login.domain.Role;
import com.study.studyproject.member.domain.Member;
import com.study.studyproject.member.dto.AdminDashBoardResponseDto;
import com.study.studyproject.member.dto.MemberListRequestDto;
import com.study.studyproject.member.dto.UserInfoResponseDto;
import com.study.studyproject.member.repository.MemberRepository;
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
import static org.assertj.core.api.Assertions.*;

@SpringBootTest
@Transactional
class AdminServiceImplTest {

    @Autowired
    MemberRepository memberRepository;

    @Autowired
    BoardRepository boardRepository;


    @Autowired
    AdminServiceImpl adminService;


    @AfterEach
    void tearDown() {
        boardRepository.deleteAllInBatch();
        memberRepository.deleteAllInBatch();

    }

    @Test
    @DisplayName("사용자 검색을 입력하지 않을 경우, 사용자 전체 정보 가져온다.")
    void userAllAdmin() throws Exception {
        //given
        Member member1 = createMember("jacom2@naver.com", "!12341234", "사용자명1", "닉네임0");
        Member member2 = createMember("jacom1@naver.com", "!kimkimkim", "사용자명2", "닉네임1");
        Member member3 = createMember("jacom3@naver.com", "!123", "사용자명3", "닉네임2");
        Member member4 = createMember("jacom4@naver.com", "!1234", "사용자명4", "닉네임3");
        memberRepository.saveAll(List.of(member1,member2,member3,member4));

        PageRequest pageRequest = PageRequest.of(0, 10);
        //when

        Page<UserInfoResponseDto> userInfoResponseDtos = adminService.userInfoAll(null, pageRequest);

        //then
        List<UserInfoResponseDto> content = userInfoResponseDtos.getContent();
        assertThat(content.size()).isEqualTo(4);

    }


    @Test
    @DisplayName("사용자 검색을 할 경우, 관련 사용자들을 가져온다.")
    void getUserNameFind() throws Exception {
        //given
        Member member1 = createMember("jacom2@naver.com", "!12341234", "사용자명1", "닉네임0");
        Member member2 = createMember("jacom1@naver.com", "!kimkimkim", "사용자명2", "닉네임1");
        Member member3 = createMember("jacom3@naver.com", "!123", "사용자명3", "닉네임2");
        Member member4 = createMember("jacom4@naver.com", "!1234", "사용자명4", "닉네임3");
        Member member5 = createMember("jacom5@naver.com", "!1234", "김사랑", "닉네임4");
        Member member6 = createMember("jacom6@naver.com", "!1234", "김진수", "닉네임5");
        memberRepository.saveAll(List.of(member1,member2,member3,member4,member5,member6));

        PageRequest pageRequest = PageRequest.of(0, 10);
        //when

        Page<UserInfoResponseDto> userInfoResponseDtos = adminService.userInfoAll("사용자", pageRequest);

        //then
        List<UserInfoResponseDto> content = userInfoResponseDtos.getContent();
        assertThat(content.size()).isEqualTo(4);
        assertThat(content)
                .extracting("username", "nickname")
                .containsExactlyInAnyOrder(
                        tuple("사용자명4", "닉네임3"),
                        tuple("사용자명3", "닉네임2"),
                        tuple("사용자명2", "닉네임1"),
                        tuple("사용자명1", "닉네임0")

                );
    }



    @Test
    @DisplayName("관리자 정보와 모든 게시글페이지를 최신순으로 조회한다.")
    void adminDashAllBoard() throws Exception {
        //given
        Member adminOne = createAdmin();
        Member member1 = createMember("jacom2@naver.com", "!12341234", "사용자명1", "닉네임0");
        Board board = createBoard(member1, "제목1", "내용1", "닉네임1", CS);
        Board board1 = createBoard(member1, "제목2", "내용2", "닉네임1", CS);
        Board board2 = createBoard(member1, "제목3", "내용3", "닉네임1", 기타);

        memberRepository.saveAll(List.of(adminOne,member1));
        boardRepository.saveAll(List.of(board, board1, board2));
        MemberListRequestDto memberListRequestDto = new MemberListRequestDto();
        PageRequest pageRequest = PageRequest.of(0, 10);


        //when
        AdminDashBoardResponseDto adminDashBoardResponseDto = adminService.adminDashBoardInfo(adminOne, memberListRequestDto, pageRequest);

        //then
        List<ListResponseDto> content = adminDashBoardResponseDto.getListResponseDto().getContent();
        assertThat(adminDashBoardResponseDto.getNickname()).isEqualTo(adminOne.getNickname());
        assertThat(adminDashBoardResponseDto.getRole()).isEqualTo(adminOne.getRole());
        assertThat(content.size()).isEqualTo(3);
        assertThat(content)
                .extracting("title", "type")
                .containsExactlyInAnyOrder(
                        tuple("제목1", "CS"),
                        tuple("제목2", "CS"),
                        tuple("제목3", "기타")
                );

    }


    @Test
    @DisplayName("관리자 정보와 CS카테고리인 게시글을 조회수순으로 조회한다.")
    void adminDashGetCsBoard() throws Exception {
        //given
        Member adminOne = createAdmin();
        Member member1 = createMember("jacom2@naver.com", "!12341234", "사용자명1", "닉네임0");
        Board board = createBoard(member1, "제목1", "내용1", "닉네임1", CS);
        Board board1 = createBoard(member1, "제목2", "내용2", "닉네임1", CS);
        Board board2 = createBoard(member1, "제목3", "내용3", "닉네임1", 기타);
        board2.updateViewCnt();

        memberRepository.saveAll(List.of(adminOne,member1));
        boardRepository.saveAll(List.of(board, board1, board2));
        MemberListRequestDto memberListRequestDto = new MemberListRequestDto(Recruit.모집중, CS,0,null);
        PageRequest pageRequest = PageRequest.of(0, 10);


        //when
        AdminDashBoardResponseDto adminDashBoardResponseDto = adminService.adminDashBoardInfo(adminOne, memberListRequestDto, pageRequest);

        //then
        List<ListResponseDto> content = adminDashBoardResponseDto.getListResponseDto().getContent();
        assertThat(adminDashBoardResponseDto.getNickname()).isEqualTo(adminOne.getNickname());
        assertThat(content.size()).isEqualTo(2);
        assertThat(content)
                .extracting("title", "type")
                .contains(
                        tuple("제목2", "CS"),
                        tuple("제목1", "CS")
                );

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

    private static Member createAdmin() {
        return Member.builder()
                .role(Role.ROLE_ADMIN)
                .username("김관리")
                .email("admin@naver.com")
                .nickname("admin")
                .password("1234")
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