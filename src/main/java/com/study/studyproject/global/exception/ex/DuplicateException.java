package com.study.studyproject.global.exception.ex;

public class DuplicateException extends BusinessException {
    public DuplicateException(ErrorCode errorCode) {
        super(errorCode);
    }
}