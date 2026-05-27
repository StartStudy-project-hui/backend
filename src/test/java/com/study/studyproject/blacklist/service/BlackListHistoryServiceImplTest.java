package com.study.studyproject.blacklist.service;

import com.study.studyproject.blacklist.domain.BlackListHistory;
import com.study.studyproject.blacklist.domain.BlackType;
import com.study.studyproject.blacklist.domain.BlacklistStatus;
import com.study.studyproject.blacklist.dto.request.BlackListHistoryMainRequestDto;
import com.study.studyproject.blacklist.dto.response.BlacklistHistoryAdminResponseDto;
import com.study.studyproject.blacklist.dto.response.BlacklistHistoryMemberResponseDto;
import com.study.studyproject.blacklist.repository.blacklisthistory.BlackListHistoryRepository;
import com.study.studyproject.global.hash.HashUtil;
import com.study.studyproject.login.domain.Role;
import com.study.studyproject.member.domain.Member;
import com.study.studyproject.member.repository.MemberRepository;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Slice;

import java.util.List;


import static com.study.studyproject.blacklist.domain.BlacklistAction.*;
import static com.study.studyproject.blacklist.domain.BlacklistAction.REGISTER;
import static com.study.studyproject.login.domain.Role.ROLE_USER;
import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Transactional
class BlackListHistoryServiceImplTest {

    @Autowired
     MemberRepository memberRepository;

    @Autowired
     BlackListHistoryRepository blackListHistoryRepository;



    @Autowired
    BlackListHistoryServiceImpl blackListHistoryService;


    @AfterEach
    void tearDown() {
        memberRepository.deleteAllInBatch();
        blackListHistoryRepository.deleteAllInBatch();

    }

    @Test
    @DisplayName(("메인 히스토리 리스트 보여준다."))
    void blackhistoryMainList() {
        Member member1 = createMember("jacom2@naver.com", "1234", "사용자명1", "닉네임1", Role.ROLE_ADMIN);
        Member member2 = createMember("jacom3@naver.com", "1234", "사용자명2", "닉네임2", ROLE_USER);
        Member member3 = createMember("jacom4@naver.com", "1234", "사용자명3", "닉네임3", ROLE_USER);
        String hash1 = HashUtil.sha256(member1.getEmail().address());
        String hash2 = HashUtil.sha256(member2.getEmail().address());
        String hash3 = HashUtil.sha256(member3.getEmail().address());
        BlackListHistory blackListHistory = BlackListHistory  .save(REGISTER,null,hash1, BlackType.EMAIL, "pishing", BlacklistStatus.ACTIVE);
        BlackListHistory blackListHistory2 = BlackListHistory.save(REGISTER,null,hash2, BlackType.EMAIL, "pishing", BlacklistStatus.ACTIVE);
        BlackListHistory blackListHistory3 = BlackListHistory.save(REGISTER,null,hash3, BlackType.EMAIL, "pishing", BlacklistStatus.ACTIVE);
        BlackListHistory update1 = BlackListHistory.save(UPDATE,null,hash1, BlackType.EMAIL, "욕설", BlacklistStatus.PERMANENT);
        BlackListHistory update2 = BlackListHistory.save(UPDATE,null,hash2, BlackType.EMAIL, "욕설2", BlacklistStatus.PERMANENT);
        BlackListHistory update3 = BlackListHistory.save(UPDATE,null,hash3, BlackType.EMAIL, "욕설3", BlacklistStatus.PERMANENT);
        memberRepository.saveAll(List.of(member1, member2, member3));
        blackListHistoryRepository.saveAll(List.of(blackListHistory, blackListHistory2, blackListHistory3,update1,update2,update3));


        BlackListHistoryMainRequestDto dto = new BlackListHistoryMainRequestDto();
        PageRequest pageRequest = PageRequest.of(0, 10);

        Page<BlacklistHistoryAdminResponseDto> list = blackListHistoryService.blackHistoryAdminList(dto, pageRequest);

        List<BlacklistHistoryAdminResponseDto> contents = list.getContent();

        assertThat(contents).hasSize(6)
                .extracting("action")
                .containsExactlyInAnyOrder(REGISTER, REGISTER, REGISTER, UPDATE, UPDATE, UPDATE);
    }

    @Test
    @DisplayName("특정 닉네임을 조회한 경우, 해당 히스토리를 가져온다.")
    void findByNickname() {
        Member member1 = createMember("jacom2@naver.com", "1234", "사용자명1", "닉네임1", Role.ROLE_ADMIN);
        String hash1 = HashUtil.sha256(member1.getEmail().address());
        BlackListHistory blackListHistory = BlackListHistory.save( REGISTER,null,hash1, BlackType.EMAIL, "pishing", BlacklistStatus.ACTIVE);
        BlackListHistory update1 = BlackListHistory.save(UPDATE,null,hash1, BlackType.EMAIL, "욕설1", BlacklistStatus.PERMANENT);
        BlackListHistory update2 = BlackListHistory.save(UPDATE,null,hash1, BlackType.EMAIL, "욕설2", BlacklistStatus.PERMANENT);
        memberRepository.save(member1);
        blackListHistoryRepository.saveAll(List.of(blackListHistory, update1,update2));
        PageRequest pageRequest = PageRequest.of(0, 10);

        Slice<BlacklistHistoryMemberResponseDto> findByIdSliceList = blackListHistoryService.findSliceBlackHistoryById(member1.getId(), pageRequest);

        assertThat(findByIdSliceList).hasSize(3)
                .extracting("action")
                .containsExactly( UPDATE,UPDATE,REGISTER);

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