package com.study.studyproject.ad.domain;

public enum AdChangeType {
    CREATE("생성"),
    UPDATE("수정"),
    EXTEND("기간 연장"),
    CANCEL("취소");
    private String description;
    AdChangeType(String description) {
        this.description = description;
    }

}
