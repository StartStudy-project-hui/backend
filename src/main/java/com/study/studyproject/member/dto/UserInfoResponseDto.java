package com.study.studyproject.member.dto;

import com.study.studyproject.member.domain.Email;
import com.study.studyproject.member.domain.Member;
import com.study.studyproject.member.domain.SocialType;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

@Data
public class UserInfoResponseDto {


    @Schema(description = "순서", defaultValue = "1")
    Long seq;

    @Schema(description = "사용자 이름", defaultValue = "김미")
    String username;

    @Schema(description = "사용자 닉네임", defaultValue = "jac")
    String nickname;

    @Schema(description = "사용자 이메일", defaultValue = "jac@naver.com")
    Email email;

    @Schema(description = "사용자 역할", defaultValue = "ROLE_USER")
    String role;

    @Schema(description = "소셜 로그인", defaultValue = "KAKAO")
    SocialType socialType;

    @Builder
    public UserInfoResponseDto(Long seq, String username, String nickname, Email email, String role,SocialType socialType) {
        this.seq = seq;
        this.username = username;
        this.nickname = nickname;
        this.email = email;
        this.role = role;
        this.socialType = socialType;
    }




    public static UserInfoResponseDto of(Member member) {
        return UserInfoResponseDto.builder()
                .seq(member.getId())
                .username(member.getUsername())
                .nickname(member.getNickname())
                .email(member.getEmail())
                .role(member.getRole().name())
                .socialType(member.getSocialType())
                .build();
    }


}
