package com.study.studyproject.ad.domain;

import jakarta.persistence.Embeddable;

@Embeddable
public enum BannerPosition {
    MAIN_TOP("메인 위"),
    MAIN_MIDDLE("메인 아래"), SIDE("사이드"),POPUP("팝업");
    private String description;
    BannerPosition(String description) {
        this.description = description;
    }

}
