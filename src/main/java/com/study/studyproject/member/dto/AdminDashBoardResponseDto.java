package com.study.studyproject.member.dto;

import com.study.studyproject.member.domain.Member;
import com.study.studyproject.auth.domain.Role;
import com.study.studyproject.list.dto.ListResponseDto;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.domain.Page;

@Getter
@NoArgsConstructor
public class AdminDashBoardResponseDto {
    @Schema(description = "이름", defaultValue = "김관리")
    private String name;
    @Schema(description = "닉네임", defaultValue = "admin")
    private String nickname;

    @Schema(description = "이메일", defaultValue = "admin@naver.com")
    private String email;
    @Schema(description = "역할")
    private Role role;
    Page<ListResponseDto> listResponseDto;


    @Builder
    public AdminDashBoardResponseDto(String name, String nickname, String email, Role role, Page<ListResponseDto> listResponseDto) {
        this.name = name;
        this.nickname = nickname;
        this.email = email;
        this.role = role;
        this.listResponseDto = listResponseDto;
    }

    public static AdminDashBoardResponseDto of(Member member,   Page<ListResponseDto> listResponseDtos) {
        return AdminDashBoardResponseDto.builder()
                .name(member.getUsername())
                .nickname(member.getNickname())
                .role(member.getRole())
                .email(member.getEmail().address())
                .listResponseDto(listResponseDtos)
                .build();

    }

}
