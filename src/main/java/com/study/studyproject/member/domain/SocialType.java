package com.study.studyproject.member.domain;

import java.util.Arrays;

public enum SocialType {
    NAVER("naver", "네이버"),
    KAKAO("kakao", "카카오");

    private final String registrationId;
    private final String displayName;

    SocialType(String registrationId, String displayName) {
        this.registrationId = registrationId;
        this.displayName = displayName;
    }

    public static SocialType fromRegistrationId(String registrationId) {
        return Arrays.stream(values())
                .filter(socialType -> socialType.registrationId.equalsIgnoreCase(registrationId))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException(
                        "지원하지 않는 소셜 로그인 타입입니다. registrationId=" + registrationId
                ));

    }


}
