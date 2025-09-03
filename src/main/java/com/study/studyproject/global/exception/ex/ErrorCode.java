package com.study.studyproject.global.exception.ex;

import lombok.Getter;
import org.springframework.http.HttpStatus;

import static org.springframework.http.HttpStatus.*;

@Getter
public enum ErrorCode {
    //로그인 및 회원가입
    MEMBER_DUPLICATED("이미 회원가입 하였습니다.", CONFLICT),
    NOT_FOUND_MEMBER("사용자를 찾지 못했습니다.", NOT_FOUND),
    NOT_FOUND_PASSWORD("비밀번호가 틀립니다.", CONFLICT),
    NICKNAME_DUPLICATED("이미 닉네임이 존재합니다. 다른 닉네임으로 변경해주세요.", CONFLICT),


    //게시글
    NOT_FOUND_BOARD("게시글이 없습니다.", NOT_FOUND),
    UNABLE_DELETE_BOARD("게시글을 삭제 할 수 없습니다.", CONFLICT),
    NOT_FOUND_REPLY("댓글을 찾을 수 없습니다.", NOT_FOUND),

    //관심글
    POST_LIKE_DUPLICATED("관심글이 이미 추가하였습니다.", BAD_REQUEST),

    //토큰
    TOKEN_EXPIRED( "유효 기간이 만료된 토큰입니다.", UNAUTHORIZED),
    UNABLE_ACCESS( "접근 권한이 없습니다.", FORBIDDEN),
    EXPIRED_PERIOD_TOKEN("토큰이 만료되었습니다. 다시 로그인해주세요. ", UNAUTHORIZED ),
    INVALID_REFRESH_TOKEN("올바르지 않은 형식의 RefreshToken입니다 다시 로그인해주세요. ", UNAUTHORIZED ),


    //그 외 오류
    NOT_FOUND_PAGE("페이지가 없습니다.", NOT_FOUND ),
    NOT_FOUND_VALUE("값을 찾을 수 없습니다.",NOT_FOUND),
    INTERNAL_SEVER_ERROR("서버 에러가 발생하였습니다. 관리자에게 문의해 주세요.", INTERNAL_SERVER_ERROR );

    private final HttpStatus status;
    private final String message;

    ErrorCode(String message, HttpStatus status) {
        this.message = message;
        this.status = status;
    }

}
