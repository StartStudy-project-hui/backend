package com.study.studyproject.global.response;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum ResultCode {
    // ===== SUCCESS =====
    BOARD_UPDATED(HttpStatus.OK, "BOARD_UPDATED", "모집 구분 변경 완료");



    private final HttpStatus status;
    private final String code;
    private final String message;


}
