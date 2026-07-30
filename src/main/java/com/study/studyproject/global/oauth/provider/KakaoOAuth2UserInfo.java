package com.study.studyproject.global.oauth.provider;

import java.util.Map;

public class KakaoOAuth2UserInfo implements OAuth2UserInfo {


    private final Map<String, Object> attributes;

    public KakaoOAuth2UserInfo(Map<String, Object> attributes) {
        this.attributes = attributes;
    }

    @Override
    public String getId() {
        return getStringValue(attributes, "id");
    }


    @Override
    public String getEmail() {
        Map<String, Object> account = getKakaoAccount();

        if (account == null) {
            return null;
        }

        return getStringValue(account, "email");
    }

    @Override
    public String getName() {
        return getNickname();
    }

    @Override
    public String getNickname() {
        Map<String, Object> profile = getProfile();
        if (profile == null) {
            return null;
        }

        return getStringValue(profile, "nickname");
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> getProfile() {
        Map<String, Object> account = getKakaoAccount();
        if (account == null) {
            return null;
        }

        Object profile = account.get("profile");

        if (!(profile instanceof Map)) {
            return null;
        }

        return (Map<String, Object>) profile;
    }
    @SuppressWarnings("unchecked")
    private Map<String, Object> getKakaoAccount() {
        Map<String, Object> kakaoAccount = (Map<String, Object>) attributes.get("kakao_account");
        if (!(kakaoAccount instanceof Map)) {
            return null;
        }
        return (Map<String, Object>) kakaoAccount;
    }

    private String getStringValue(Map<String, Object> source, String key) {
        Object value = source.get(key);

        if (value == null) {
            return null;
        }

        return String.valueOf(value);
    }

}
