package com.study.studyproject.login.dto;

import com.study.studyproject.member.domain.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class SignRequest {

    @NotBlank(message = "이름을 입력해주세요")
    private String name;

    @NotBlank(message = "이메일을 입력해주세요")
    @jakarta.validation.constraints.Email
    private String email;

    @NotBlank(message = "닉네임을 입력해주세요")
    private String nickname;

    @NotBlank(message = "비밀번호를 입력해주세요")
    private String pwd;

    @NotBlank(message = "비밀번호 확인을 입력해주세요")
    private String checkPwd;

    @Builder
    public SignRequest(String name, String email, String nickname, String pwd, String checkPwd) {
        this.name = name;
        this.email = email;
        this.nickname = nickname;
        this.pwd = pwd;
        this.checkPwd = checkPwd;
    }

    public boolean isNotEqualsCheckPwd(){
        if(pwd != null || checkPwd != null){
            return !pwd.equals(checkPwd);
        }
        return false;
    }

}
