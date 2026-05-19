package com.study.studyproject.global.oauth;

import com.study.studyproject.global.oauth.provider.KakaoOAuth2UserInfo;
import com.study.studyproject.global.oauth.provider.NaverOAuth2UserInfo;
import com.study.studyproject.global.oauth.provider.OAuth2UserInfo;
import com.study.studyproject.member.domain.SocialType;
import lombok.Builder;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.util.Map;

import static com.study.studyproject.member.domain.SocialType.KAKAO;
import static com.study.studyproject.member.domain.SocialType.NAVER;

@Getter
@Slf4j
public class OAuthAttributes {
    private String nameAttributeKey;
    private OAuth2UserInfo oauth2UserInfo;

    @Builder
    public OAuthAttributes(String nameAttributeKey, OAuth2UserInfo oauth2UserInfo) {
        this.nameAttributeKey = nameAttributeKey;
        this.oauth2UserInfo = oauth2UserInfo;
    }


    public static OAuthAttributes of(SocialType socialType,
                                     String userNameAttributeName, Map<String, Object> attributes) {

        if (socialType.equals(NAVER)) {

            return ofNaver(userNameAttributeName, (Map<String, Object>)attributes.get(userNameAttributeName));
        }
        if (socialType.equals(KAKAO)) {
            return ofKakao(userNameAttributeName, attributes);
        }
        throw new IllegalArgumentException("지원하지 않는 소셜 로그인 타입입니다. socialType=" + socialType);
    }


    public static OAuthAttributes ofKakao(String userNameAttributeName, Map<String, Object> attributes) {
        return OAuthAttributes.builder()
                .nameAttributeKey(userNameAttributeName)
                .oauth2UserInfo(new KakaoOAuth2UserInfo(attributes))
                .build();
    }


    public static OAuthAttributes ofNaver(String userNameAttributeName, Map<String, Object> attributes) {
        return com.study.studyproject.global.oauth.OAuthAttributes.builder()
                .nameAttributeKey(userNameAttributeName)
                .oauth2UserInfo(new NaverOAuth2UserInfo(attributes))
                .build();
    }


}
