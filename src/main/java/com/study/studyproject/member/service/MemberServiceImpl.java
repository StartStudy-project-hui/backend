package com.study.studyproject.member.service;

import com.study.studyproject.global.exception.ex.DuplicateException;
import com.study.studyproject.global.exception.ex.NotFoundException;
import com.study.studyproject.global.oauth.OAuthAttributes;
import com.study.studyproject.list.dto.ListResponseDto;
import com.study.studyproject.member.domain.Member;
import com.study.studyproject.global.GlobalResultDto;
import com.study.studyproject.member.domain.SocialType;
import com.study.studyproject.member.dto.MemberListRequestDto;
import com.study.studyproject.member.dto.MemberUpdateResDto;
import com.study.studyproject.member.dto.UserInfoResponseDto;
import com.study.studyproject.member.repository.MemberRepository;
import com.study.studyproject.member.repository.MyPagePostLikeQueryRepository;
import com.study.studyproject.member.repository.MyPageQueryRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import static com.study.studyproject.global.exception.ex.ErrorCode.*;
import static com.study.studyproject.member.domain.Member.toEntity;

@RequiredArgsConstructor
@Service
@org.springframework.transaction.annotation.Transactional
@Slf4j
public class MemberServiceImpl implements MemberService{

    private final MemberRepository memberRepository;
    private final MyPageQueryRepository myPageQueryRepository;
    private final MyPagePostLikeQueryRepository myPagePostLikeQueryRepository;
    private final String GET_MEMBER = "member";

    //사용자 정보조회
    @Override
    @Transactional(readOnly  = true)
    public UserInfoResponseDto userInfoService(Member member) {
        return UserInfoResponseDto.of(member);

    }

    @Override
    public GlobalResultDto userInfoUpdate(Member member , MemberUpdateResDto memberUpdateResDto) {

        if (memberRepository.existsByNickname(memberUpdateResDto.getNickname())) {
            throw new DuplicateException(NICKNAME_DUPLICATED);
        }
        Member getMember = memberRepository
                .findById(member.getId())
                .orElseThrow(() -> new NotFoundException(NOT_FOUND_MEMBER));

        getMember.updateMemberInfo(memberUpdateResDto);
        return new GlobalResultDto("사용자 수정 성공", HttpStatus.OK.value());


    }

    @Override
    public Page<ListResponseDto> listMember(Long memberId, MemberListRequestDto memberListRequestDto, Pageable pageable) {
        return myPageQueryRepository.MyPageListPage(memberListRequestDto, pageable,memberId, GET_MEMBER);
    }

    @Override
    public Member getOrCreateUser(OAuthAttributes attributes, SocialType socialType) {
        Member findMember = memberRepository.findBySocialTypeAndSocialId(socialType,
                attributes.getOauth2UserInfo().getId()).orElse(null);
        if (notFindMember(findMember)) {
            return saveMember(attributes, socialType);
        }
        return findMember;
    }

    private static boolean notFindMember(Member findMember) {
        return findMember == null;
    }

    private Member saveMember(OAuthAttributes attributes, SocialType socialType) {
        Member createMember = toEntity(socialType, attributes.getOauth2UserInfo());
        return memberRepository.save(createMember);
    }
    public Page<ListResponseDto> postLikeBoard(Long memberId, MemberListRequestDto memberListRequestDto, Pageable pageable) {
        return myPagePostLikeQueryRepository.MyPageListPage(memberListRequestDto, pageable,memberId);
    }
}


