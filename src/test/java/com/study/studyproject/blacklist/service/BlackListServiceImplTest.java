package com.study.studyproject.blacklist.service;

import com.study.studyproject.blacklist.domain.*;
import com.study.studyproject.blacklist.dto.request.BlackListCreateRequestDto;
import com.study.studyproject.blacklist.dto.request.BlackListUpdateRequestDto;
import com.study.studyproject.blacklist.repository.blacklist.BlackListRepository;
import com.study.studyproject.blacklist.repository.blacklisthistory.BlackListHistoryRepository;
import com.study.studyproject.global.GlobalResultDto;
import com.study.studyproject.global.hash.HashUtil;
import com.study.studyproject.global.exception.ex.NotFoundException;
import com.study.studyproject.login.domain.Role;
import com.study.studyproject.member.domain.Member;
import com.study.studyproject.member.repository.MemberRepository;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static com.study.studyproject.global.exception.ex.ErrorCode.NOT_FOUND_MEMBER;
import static org.assertj.core.api.Assertions.*;

@SpringBootTest
@Transactional
@ActiveProfiles("test")
@WithMockUser(username = "testUser")
class BlackListServiceImplTest {


    @Autowired
    BlackListHistoryRepository blackListHistoryRepository;

    @Autowired
    MemberRepository memberRepository;

    @Autowired
    BlackListServiceImpl blackListService;

    @Autowired
    BlackListRepository blackListRepository;

    @AfterEach
    void tearDown() {
        memberRepository.deleteAllInBatch();
        blackListHistoryRepository.deleteAllInBatch();
        blackListRepository.deleteAllInBatch();
    }

    @Test
    @DisplayName("블랙리스트에 등록하면 저장이 된다.")
    void register() throws Exception {
        //given
        Member member1 = createMember("jacom2@naver.com", "1234", "사용자명1", "닉네임1", Role.ROLE_ADMIN);
        BlackListCreateRequestDto requestDto = new BlackListCreateRequestDto(member1.getEmail().address(), "pishing", 1);

        //when
        GlobalResultDto register = blackListService.registerOrUpdateBlackList(requestDto);

        //then
        List<BlackList> all = blackListRepository.findAll();
        assertThat(all).hasSize(1);
        assertThat(register.getStatusCode()).isEqualTo(200);

    }

    @Test
    @DisplayName("블랙리스트에 2번 등록하면 블랙리스트는 수정하면 저장이 된다.")
    void blackList_update() throws Exception {
        //given

        Member member1 = createMember("jacom2@naver.com", "1234", "사용자명1", "닉네임1", Role.ROLE_ADMIN);
        BlackListCreateRequestDto requestDto = new BlackListCreateRequestDto(member1.getEmail().address(),  "pishing", 1);

        //when
        GlobalResultDto register = blackListService.registerOrUpdateBlackList(requestDto);

        BlackListCreateRequestDto requestDto2 = new BlackListCreateRequestDto(member1.getEmail().address() , "변경-욕설", 2);

        //when
        GlobalResultDto res = blackListService.registerOrUpdateBlackList(requestDto2);

        //then
        List<BlackList> blackList = blackListRepository.findAll();

        List<BlackListHistory> history = blackListHistoryRepository.findByHashValue(blackList.get(0).getHashValue());



        assertThat(blackList).hasSize(1);
        assertThat(blackList.get(0).getReason()).isEqualTo("변경-욕설");
        assertThat(history.size()).isEqualTo(2);
        assertThat(res.getStatusCode()).isEqualTo(200);

    }


    @Test
    @DisplayName("블랙리스트의 변경 이유를 수정한다.")
    void  update() throws Exception {
        //given
        Member member1 = createMember("jacom2@naver.com", "1234", "사용자명1", "닉네임1", Role.ROLE_ADMIN);
        String hash = HashUtil.sha256(member1.getEmail().address());
        BlackList blacklist = BlackList.create( hash, "욕설");
        blackListRepository.save(blacklist);

        //when
        GlobalResultDto res = blackListService.update(blacklist.getId(), new BlackListUpdateRequestDto("피싱"));

        //then
        BlackList blackList = blackListRepository.findById(blacklist.getId()).get();
        assertThat(blackList.getReason()).isEqualTo("피싱");
    }

    @Test
    @DisplayName("블랙리스트에서 삭제하면, 블랙리스트 상태가 만료로 변하며, 블랙리스트에는 삭제된다.")
    void delete() throws Exception {
        //given
        Member member1 = createMember("jacom2@naver.com", "1234", "사용자명1", "닉네임1", Role.ROLE_ADMIN);
        String hash = HashUtil.sha256(member1.getEmail().address());
        BlackList blacklist = BlackList.create( hash, "욕설");
        blackListRepository.save(blacklist);

        // 히스토리 생성
        BlackListHistory history = BlackListHistory.save(BlacklistAction.REGISTER,blacklist, hash,BlackType.EMAIL,"reason", BlacklistStatus.ACTIVE);
        blackListHistoryRepository.save(history);

        //when
        blackListService.delete(blacklist.getId());

        //then
        assertThatThrownBy(() -> blackListRepository.findById(blacklist.getId())
                .orElseThrow(() -> new NotFoundException(NOT_FOUND_MEMBER)))
                .isInstanceOf(NotFoundException.class);

    }


    private Member createMember
            (String email, String password, String username, String nickname, Role role) {
        {
            return Member.builder()
                    .nickname(nickname)
                    .username(username)
                    .email(email)
                    .password(password)
                    .role(role).build();
        }

    }



}