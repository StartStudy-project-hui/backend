package com.study.studyproject.member.service;

import com.study.studyproject.board.domain.Board;
import com.study.studyproject.board.domain.Category;
import com.study.studyproject.board.domain.RecruitStatus;
import com.study.studyproject.board.repository.BoardRepository;
import com.study.studyproject.global.exception.ex.DuplicateException;
import com.study.studyproject.global.jwt.JwtUtil;
import com.study.studyproject.global.oauth.OAuthAttributes;
import com.study.studyproject.list.dto.ListResponseDto;
import com.study.studyproject.member.domain.Member;
import com.study.studyproject.member.domain.SocialType;
import com.study.studyproject.member.dto.MemberListRequestDto;
import com.study.studyproject.member.dto.MemberUpdateResDto;
import com.study.studyproject.member.dto.UserInfoResponseDto;
import com.study.studyproject.member.repository.MemberRepository;
import com.study.studyproject.postlike.domain.PostLike;
import com.study.studyproject.postlike.repository.PostLikeRepository;
import com.study.studyproject.reply.repository.ReplyRepository;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.study.studyproject.board.domain.Category.*;
import static com.study.studyproject.auth.domain.Role.ROLE_USER;
import static org.assertj.core.api.Assertions.*;

@SpringBootTest
@Transactional
class MemberServiceImplTest {

    @Autowired
    JwtUtil jwtUtil;
    @Autowired
    BoardRepository boardRepository;

    @Autowired
    MemberServiceImpl memberService;

    @Autowired
    MemberRepository memberRepository;

    @Autowired
    PostLikeRepository postLikeRepository;

    @Autowired
    ReplyRepository replyRepository;

    @AfterEach
    void tearDown() {
        postLikeRepository.deleteAllInBatch();
        replyRepository.deleteAllInBatch();
        boardRepository.deleteAllInBatch();
        memberRepository.deleteAllInBatch();

    }

    @Test
    @DisplayName("사용자 정보를 조회한다.")
    void userInfoService() {
        //given
        Member member1 = createMember("jacom2@naver.com", "!12341234", "사용자명1", "닉네임0");
        memberRepository.save(member1);

        //when
        UserInfoResponseDto userInfoResponseDto = memberService.userInfoService(member1);

        //then
        assertThat(userInfoResponseDto.getEmail()).isEqualTo(member1.getEmail().address());
        assertThat(userInfoResponseDto.getSeq()).isEqualTo(member1.getId());

    }

    @Test
    @DisplayName("사용자 정보 수정한다.")
    void userInfoUpdate() {
        //given
        Member member1 = createMember("jacom2@naver.com", "!12341234", "사용자명1", "닉네임0");
        memberRepository.save(member1);
        MemberUpdateResDto memberUpdateResDto = new MemberUpdateResDto("수정이름", "수정닉네임");

        //when
        memberService.userInfoUpdate(member1, memberUpdateResDto);

        //then
        Member getMember = memberRepository.findById(member1.getId()).get();
        assertThat(getMember.getNickname()).isEqualTo(memberUpdateResDto.getNickname());
        assertThat(getMember.getUsername()).isEqualTo(memberUpdateResDto.getUsername());
    }

    @Test
    @DisplayName("사용자가 작성한 모든 게시글을 최신순으로 조회한다.")
    void listMember() {
        //given
        Member member1 = createMember("jacom2@naver.com", "!12341234", "사용자명1", "닉네임0");
        Board board = createBoard(member1, "제목1", "내용1",  CS);
        Board board1 = createBoard(member1, "제목2", "내용2",  CS);
        Board board2 = createBoard(member1, "제목3", "내용3", ETC);
        memberRepository.save(member1);
        boardRepository.save(board);
        boardRepository.save(board1);
        boardRepository.save(board2);
        PageRequest pageRequest = PageRequest.of(0, 10);
        MemberListRequestDto memberListRequestDto = new MemberListRequestDto();


        //when
        Page<ListResponseDto> listResponseDtos = memberService.listMember(member1.getId(), memberListRequestDto, pageRequest);

        //then
        List<ListResponseDto> content = listResponseDtos.getContent();
        assertThat(content).hasSize(3);
        assertThat(content)
                .extracting("title", "type")
                .containsExactlyInAnyOrder(
                        tuple("제목2", "CS"),
                        tuple("제목3", "ETC"),
                        tuple("제목1", "CS")
                );
    }


    @Test
    @DisplayName("사용자가 작성한  CS카테고리 게시글을 최신순으로 조회한다.")
    void getCsCategoryListMember() throws InterruptedException {
        //given
        Member member1 = createMember("jacom2@naver.com", "!12341234", "사용자명1", "닉네임0");
        Board board = createBoard(member1, "제목1", "내용1",  CS);
        Board board1 = createBoard(member1, "제목2", "내용2",  CS);
        Board board2 = createBoard(member1, "제목3", "내용3", ETC);
        memberRepository.save(member1);
        boardRepository.save(board);
        boardRepository.save(board1);
        boardRepository.save(board2);
        PageRequest pageRequest = PageRequest.of(0, 10);
        MemberListRequestDto memberListRequestDto = new MemberListRequestDto(RecruitStatus.RECRUITING, CS,0,null);

        //when
        Page<ListResponseDto> listResponseDtos = memberService.listMember(member1.getId(), memberListRequestDto, pageRequest);

        //then
        List<ListResponseDto> content = listResponseDtos.getContent();
        assertThat(content.size()).isEqualTo(2);
        assertThat(content)
                .extracting("title", "type")
                .contains(
                        tuple("제목2", "CS"),
                        tuple("제목1", "CS")
                );

    }


    @Test
    @DisplayName("모든 관심글 게시글을 최신순으로 조회한다.")
    void postLikeBoard() throws Exception {
        //given
        Member member1 = createMember("jacom2@naver.com", "!12341234", "사용자명1", "닉네임0");
        Board board = createBoard(member1, "제목1", "내용1",  CS);
        Board board1 = createBoard(member1, "제목2", "내용2",  CS);
        Board board2 = createBoard(member1, "제목3", "내용3", ETC);
        memberRepository.save(member1);
        boardRepository.save(board);
        boardRepository.save(board1);
        boardRepository.save(board2);

        PostLike postLike1 = PostLike.create(member1, board);
        PostLike postLike2 = PostLike.create(member1, board1);
        PostLike postLike3 = PostLike.create(member1, board2);
        postLikeRepository.saveAll(List.of(postLike1, postLike2, postLike3));

        MemberListRequestDto memberListRequestDto = new MemberListRequestDto();
        PageRequest pageRequest = PageRequest.of(0, 10);


        //when
        Page<ListResponseDto> listResponseDtos = memberService.postLikeBoard(member1.getId(), memberListRequestDto, pageRequest);

        //then

        List<ListResponseDto> content = listResponseDtos.getContent();
        assertThat(content).hasSize(3);
        assertThat(content)
                .extracting("title", "type")
                .containsExactlyInAnyOrder(
                        tuple("제목3", "ETC"),
                        tuple("제목2", "CS"),
                        tuple("제목1", "CS")
                );

    }


    @Test
    @DisplayName("닉네임이 중복되면 사용자 정보 수정 시 예외가 발생한다.")
    void userInfoUpdateWithDuplicateNickname() {
        //given
        Member member1 = createMember("jacom2@naver.com", "!12341234", "사용자명1", "닉네임0");
        Member member2 = createMember("jacom3@naver.com", "!12341234", "사용자명2", "닉네임1");
        memberRepository.saveAll(List.of(member1, member2));
        MemberUpdateResDto memberUpdateResDto = new MemberUpdateResDto("수정이름", "닉네임1");

        //when & then
        assertThatThrownBy(() -> memberService.userInfoUpdate(member1, memberUpdateResDto))
                .isInstanceOf(DuplicateException.class);
    }

    @Test
    @DisplayName("소셜 로그인 사용자가 처음 로그인하면 신규로 가입된다.")
    void getOrCreateUserCreatesNewMember() {
        //given
        Map<String, Object> kakaoAccount = new HashMap<>();
        kakaoAccount.put("email", "newsocial@kakao.com");
        Map<String, Object> profile = new HashMap<>();
        profile.put("nickname", "카카오닉네임");
        kakaoAccount.put("profile", profile);

        Map<String, Object> attributes = new HashMap<>();
        attributes.put("id", "kakao-id-1");
        attributes.put("kakao_account", kakaoAccount);

        OAuthAttributes oAuthAttributes = OAuthAttributes.ofKakao("id", attributes);

        //when
        Member result = memberService.getOrCreateUser(oAuthAttributes, SocialType.KAKAO);

        //then
        assertThat(result.getId()).isNotNull();
        assertThat(result.getEmail().address()).isEqualTo("newsocial@kakao.com");
        assertThat(memberRepository.findBySocialTypeAndSocialId(SocialType.KAKAO, "kakao-id-1")).isPresent();
    }

    @Test
    @DisplayName("이미 가입된 소셜 사용자가 다시 로그인하면 기존 회원을 그대로 반환한다.")
    void getOrCreateUserReturnsExistingMember() {
        //given
        Map<String, Object> kakaoAccount = new HashMap<>();
        kakaoAccount.put("email", "existingsocial@kakao.com");
        Map<String, Object> profile = new HashMap<>();
        profile.put("nickname", "카카오닉네임");
        kakaoAccount.put("profile", profile);

        Map<String, Object> attributes = new HashMap<>();
        attributes.put("id", "kakao-id-2");
        attributes.put("kakao_account", kakaoAccount);

        OAuthAttributes oAuthAttributes = OAuthAttributes.ofKakao("id", attributes);
        Member existing = memberService.getOrCreateUser(oAuthAttributes, SocialType.KAKAO);

        //when
        Member result = memberService.getOrCreateUser(oAuthAttributes, SocialType.KAKAO);

        //then
        assertThat(result.getId()).isEqualTo(existing.getId());
        assertThat(memberRepository.findAll()).hasSize(1);
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
            Member member, String title, String content, Category category
    ) {
        return Board.builder()
                .member(member)
                .title(title)
                .content("내용")
                .category(category)
                .build();
    }
}