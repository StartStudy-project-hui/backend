package com.study.studyproject.blacklist.domain;

public enum BlacklistAction {
    REGISTER("등록"),   // 블랙리스트 등록
    UPDATE("수정"),     // 사유(reason) 변경
    DELETE("삭제"), // 블랙리스트 삭제
    EXTEND("연장");

    private final String description;
    BlacklistAction(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}
