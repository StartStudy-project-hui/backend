package com.study.studyproject.global.exception.ex;

import lombok.Getter;
import org.springframework.http.HttpStatus;

import static org.springframework.http.HttpStatus.*;

@Getter
public enum ErrorCode {
    //로그인 및 회원가입
    MEMBER_DUPLICATED(CONFLICT,"MEMBER-001","이미 회원가입 하였습니다."),
    MEMBER_NOT_FOUND(NOT_FOUND,"MEMBER-002","사용자를 찾지 못했습니다." ),
    MEMBER_PASSWORD_INVALID(CONFLICT,"MEMBER-003","비밀번호가 틀립니다." ),
    MEMBER_NICKNAME_DUPLICATED(CONFLICT,"MEMBER-004","이미 닉네임이 존재합니다. 다른 닉네임으로 변경해주세요." ),


    //게시글
    BOARD_NOT_FOUND(NOT_FOUND,"BOARD-001", "게시글이 없습니다."),
    BOARD_DELETE_NOT_ALLOWED(CONFLICT,"BOARD-002","게시글을 삭제 할 수 없습니다."),
    REPLY_NOT_FOUND(NOT_FOUND,"REPLY-003","댓글을 찾을 수 없습니다." ),

    //관심글
    POST_LIKE_DUPLICATED(BAD_REQUEST,"POST-LIKE-001","관심글이 이미 추가하였습니다."),

    //토큰
    TOKEN_EXPIRED( UNAUTHORIZED,"AUTH-001","유효 기간이 만료된 토큰입니다."),
    UNABLE_ACCESS( FORBIDDEN,"AUTH-002", "접근 권한이 없습니다."),
    EXPIRED_PERIOD_TOKEN(UNAUTHORIZED,"AUTH-003",  "토큰이 만료되었습니다. 다시 로그인해주세요."),
    INVALID_REFRESH_TOKEN(UNAUTHORIZED,"AUTH-004", "올바르지 않은 형식의 Refresh Token입니다 다시 로그인해주세요."),

    //블랙리스트
    BLACKLIST_USER(FORBIDDEN,"BLACKLIST-001","블랙리스 사용자입니다. 사이트를 이용할 수 없습니다."),


    //그 외 오류
    NOT_FOUND_PAGE(NOT_FOUND,"COMMON-001","페이지가 없습니다."),
    NOT_FOUND_VALUE(NOT_FOUND, "COMMON-002","값을 찾을 수 없습니다."),
    INTERNAL_SEVER_ERROR(INTERNAL_SERVER_ERROR,"COMMON-003","서버 에러가 발생하였습니다. 관리자에게 문의해 주세요."),
    MISSING_REQUEST_PARAM(HttpStatus.BAD_REQUEST, "COMMON-004", "%s은(는) 필수 요청 파라미터입니다.");

    private final HttpStatus status;
    private final String code;
    private final String message;

    ErrorCode(HttpStatus status, String code, String message) {
        this.status = status;
        this.code = code;
        this.message = message;
    }

    public String getMessage(String fieldName) {
        return String.format(this.message, fieldName);
    }
}
