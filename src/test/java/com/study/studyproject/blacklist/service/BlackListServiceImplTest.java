package com.study.studyproject.blacklist.service;

import com.study.studyproject.blacklist.domain.*;
import com.study.studyproject.blacklist.dto.request.BlackListCreateRequestDto;
import com.study.studyproject.blacklist.dto.request.BlackListUpdateRequestDto;
import com.study.studyproject.blacklist.repository.blacklist.BlackListRepository;
import com.study.studyproject.blacklist.repository.blacklisthistory.BlackListHistoryRepository;
import com.study.studyproject.global.GlobalResultDto;
import com.study.studyproject.global.hash.HashUtil;
import com.study.studyproject.global.exception.ex.NotFoundException;
import com.study.studyproject.auth.domain.Role;
import com.study.studyproject.member.domain.Member;
import com.study.studyproject.member.repository.MemberRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.RedisTemplate;
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

    @Autowired
    RedisTemplate<String, Object> redisTemplate;

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


    @Test
    @DisplayName("블랙리스트 등록 시 캐시가 삭제되어 다음 조회에서 최신 상태를 반영한다")
    void register_evictsCache() throws Exception {
        //given
        Member member1 = createMember("jacom2@naver.com", "1234", "사용자명1", "닉네임1", Role.ROLE_ADMIN);
        String hash = HashUtil.sha256(member1.getEmail().address());
        String cacheKey = BlackList.BLACKLIST_CACHE_KEY_PREFIX + hash;

        // 이전 요청에서 "차단 아님"으로 캐시되어 있다고 가정
        redisTemplate.opsForValue().set(cacheKey, "false");
        BlackListCreateRequestDto requestDto = new BlackListCreateRequestDto(member1.getEmail().address(), "pishing", 1);

        //when
        blackListService.registerOrUpdateBlackList(requestDto);

        //then
        assertThat(redisTemplate.hasKey(cacheKey)).isFalse();
    }

    @Test
    @DisplayName("캐시에 값이 있으면 DB에 실제 블랙리스트 기록이 없어도 캐시된 값을 그대로 반환한다.")
    void isBlocked_cacheHit() throws Exception {
        //given
        String email = "isblocked-cachehit-test@naver.com";
        String hash = HashUtil.sha256(email);
        String cacheKey = BlackList.BLACKLIST_CACHE_KEY_PREFIX + hash;

        try {
            // DB엔 블랙리스트 기록이 없지만, 캐시엔 차단됨으로 미리 저장되어 있다고 가정
            redisTemplate.opsForValue().set(cacheKey, "true");

            //when
            boolean blocked = blackListService.isBlocked(email);

            //then
            assertThat(blocked).isTrue();
        } finally {
            redisTemplate.delete(cacheKey);
        }
    }

    @Test
    @DisplayName("캐시가 비어있으면 DB를 조회해 결과를 반환하고, 이후 조회를 위해 캐시를 채운다.")
    void isBlocked_cacheMiss() throws Exception {
        //given
        String email = "isblocked-cachemiss-test@naver.com";
        String hash = HashUtil.sha256(email);
        String cacheKey = BlackList.BLACKLIST_CACHE_KEY_PREFIX + hash;
        BlackList blacklist = BlackList.create(hash, "욕설");
        blacklist.makePermanent(); // 영구정지 상태여야 isBlocked()가 true를 반환한다
        blackListRepository.save(blacklist);

        try {
            //when
            boolean blocked = blackListService.isBlocked(email);

            //then
            assertThat(blocked).isTrue();
            assertThat(redisTemplate.hasKey(cacheKey)).isTrue();
        } finally {
            redisTemplate.delete(cacheKey);
        }
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