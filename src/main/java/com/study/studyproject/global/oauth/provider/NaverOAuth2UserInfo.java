package com.study.studyproject.global.oauth.provider;

import lombok.extern.slf4j.Slf4j;

import java.util.Map;

@Slf4j
public class NaverOAuth2UserInfo implements OAuth2UserInfo {


    private final Map<String, Object> attributes;

    public NaverOAuth2UserInfo(Map<String, Object> attributes) {
        this.attributes = attributes;
    }


    @Override
    public String getId() {
        return getStringValue("id");
    }

    @Override
    public String getEmail() {
        return getStringValue("email");
    }

    @Override
    public String getName() {
        return getStringValue("name");
    }

    @Override
    public String getNickname() {
        return getStringValue("nickname");
    }

    private String getStringValue(String key) {
        Object value = attributes.get(key);

        if (value == null) {
            return null;
        }

        return String.valueOf(value);
    }
}
