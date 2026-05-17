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
        return  (String) attributes.get("id");
    }

    @Override
    public String getEmail() {
        return (String) attributes.get("email");
    }

    @Override
    public String getName() {
        return (String) attributes.get("name");
    }

    @Override
    public String getNickname() {
        return (String) attributes.get("nickname");
    }
}
