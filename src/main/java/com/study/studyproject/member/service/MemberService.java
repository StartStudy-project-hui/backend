package com.study.studyproject.member.service;


import com.study.studyproject.global.oauth.OAuthAttributes;
import com.study.studyproject.member.domain.Member;
import com.study.studyproject.main.dto.ListResponseDto;
import com.study.studyproject.global.GlobalResultDto;
import com.study.studyproject.member.domain.SocialType;
import com.study.studyproject.member.dto.MemberListRequestDto;
import com.study.studyproject.member.dto.MemberUpdateResDto;
import com.study.studyproject.member.dto.UserInfoResponseDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface MemberService {


    //사용자 정보 조회
    UserInfoResponseDto userInfoService(Member member);


    //수정
    GlobalResultDto userInfoUpdate(Member member , MemberUpdateResDto memberUpdateResDto);

    Page<ListResponseDto> listMember(Long token, MemberListRequestDto memberListRequestDto, Pageable pageable);


    Member getOrCreateUser(OAuthAttributes attributes, SocialType socialType);


}
