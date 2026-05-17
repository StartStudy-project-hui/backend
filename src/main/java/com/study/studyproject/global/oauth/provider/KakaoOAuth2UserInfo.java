package com.study.studyproject.global.oauth.provider;

import java.util.Map;

public class KakaoOAuth2UserInfo implements OAuth2UserInfo {


    private final Map<String, Object> attributes;

    public KakaoOAuth2UserInfo(Map<String, Object> attributes) {
        this.attributes = attributes;
    }

    @Override
    public String getId() {
        return String.valueOf(attributes.get("id"));
    }


    @Override
    public String getEmail() {
        Map<String, Object> account = (Map<String, Object>) attributes.get("kakao_account");
        return String.valueOf(account.get("email"));
    }

    @Override
    public String getName() {
        Map<String, Object> account = (Map<String, Object>) attributes.get("kakao_account");
        Map<String, Object> profile = (Map<String, Object>) account.get("profile");
        if (account == null || profile == null) {
            return null;
        }
        return (String) profile.get("nickname");
    }

    @Override
    public String getNickname() {
        Map<String, Object> account = (Map<String, Object>) attributes.get("kakao_account");
        Map<String, Object> profile = (Map<String, Object>) account.get("profile");
        if (account == null || profile == null) {
            return null;
        }
        return (String) profile.get("nickname");

    }
}
