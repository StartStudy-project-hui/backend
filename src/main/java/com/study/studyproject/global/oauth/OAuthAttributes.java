package com.study.studyproject.global.oauth;

import com.study.studyproject.global.oauth.provider.KakaoOAuth2UserInfo;
import com.study.studyproject.global.oauth.provider.NaverOAuth2UserInfo;
import com.study.studyproject.global.oauth.provider.OAuth2UserInfo;
import com.study.studyproject.member.domain.SocialType;
import lombok.Builder;
import lombok.Getter;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.OAuth2Error;

import java.util.Map;

import static com.study.studyproject.member.domain.SocialType.KAKAO;
import static com.study.studyproject.member.domain.SocialType.NAVER;

@Getter
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
            Object naverResponse = attributes.get(userNameAttributeName);
            if (!(naverResponse instanceof Map)) {
                throw new OAuth2AuthenticationException(new OAuth2Error("invalid_naver_response"),
                        "네이버 응답에서 사용자 정보를 찾을 수 없습니다.");
            }
            return ofNaver(userNameAttributeName, (Map<String, Object>) naverResponse);
        }
        if (socialType.equals(KAKAO)) {
            return ofKakao(userNameAttributeName, attributes);
        }
        throw new OAuth2AuthenticationException(new OAuth2Error("invalid_social_type"),
                "지원하지 않는 소셜 로그인 타입입니다. socialType=" + socialType);
    }


    public static OAuthAttributes ofKakao(String userNameAttributeName, Map<String, Object> attributes) {
        return OAuthAttributes.builder()
                .nameAttributeKey(userNameAttributeName)
                .oauth2UserInfo(new KakaoOAuth2UserInfo(attributes))
                .build();
    }


    public static OAuthAttributes ofNaver(String userNameAttributeName, Map<String, Object> attributes) {
        return OAuthAttributes.builder()
                .nameAttributeKey(userNameAttributeName)
                .oauth2UserInfo(new NaverOAuth2UserInfo(attributes))
                .build();
    }


}
