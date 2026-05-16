package com.study.studyproject.member.service;


import com.study.studyproject.global.GlobalResultDto;
import com.study.studyproject.member.dto.UserInfoResponseDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface AdminService {

    Page<UserInfoResponseDto> userInfoAll(String username,Pageable pageable);

    GlobalResultDto adminDeleteBoard(Long boardId);

    }
