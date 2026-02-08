package com.study.studyproject.ad.domain;

public enum AdStatus {

    ACTIVE("진행"), EXPIRED("만료") , TERMINATED("중단"),EXTENDED("연장");

    private String description;
    AdStatus(String description) {
        this.description = description;
    }
}
