package com.study.studyproject.global.exception;

import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import static com.study.studyproject.global.exception.ex.ErrorCode.INTERNAL_SEVER_ERROR;

@Data
@NoArgsConstructor
public class ExceptionResponse {
    private int status;
    private String code;
    private String message;

    @Builder
    public ExceptionResponse(int status, String message,String code) {
        this.status = status;
        this.message = message;
        this.code = code;
    }

    public static ExceptionResponse of(int status, String message, String code) {
        return ExceptionResponse.builder()
                .message(message)
                .status(status)
                .code(code)
                .build();
    }

}
