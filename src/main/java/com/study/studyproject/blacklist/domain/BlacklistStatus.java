package com.study.studyproject.blacklist.domain;

public enum BlacklistStatus {
    PERMANENT("영구정지"),
    ACTIVE("활성"),
    EXPIRED("만료");

    private final String description;
    BlacklistStatus(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}
