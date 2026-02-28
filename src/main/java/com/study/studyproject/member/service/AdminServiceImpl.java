package com.study.studyproject.member.service;

import com.study.studyproject.member.domain.Member;
import com.study.studyproject.main.dto.ListResponseDto;
import com.study.studyproject.member.dto.AdminDashBoardResponseDto;
import com.study.studyproject.member.dto.MemberListRequestDto;
import com.study.studyproject.member.dto.UserInfoResponseDto;
import com.study.studyproject.member.repository.MemberRepository;
import com.study.studyproject.member.repository.MyPageQueryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class AdminServiceImpl implements AdminService{

    private final MemberRepository memberRepository;
    private final MyPageQueryRepository myPageQueryRepository;
    private final String GET_ADMIN = "Admin";

    @Override
    public Page<UserInfoResponseDto> userInfoAll(String username, Pageable pageable) {
        List<UserInfoResponseDto> contents = memberRepository.getContent(username,pageable).stream().map(UserInfoResponseDto::of).collect(Collectors.toList());
        return memberRepository.adminUserBoardList(username,contents, pageable);
    }

    public AdminDashBoardResponseDto adminDashBoardInfo(Member member, MemberListRequestDto memberListRequestDto, Pageable pageable) {
        Page<ListResponseDto> listResponseDtos = myPageQueryRepository.MyPageListPage(memberListRequestDto, pageable, null, GET_ADMIN);
        return AdminDashBoardResponseDto.of(member, listResponseDtos);
    }
}

