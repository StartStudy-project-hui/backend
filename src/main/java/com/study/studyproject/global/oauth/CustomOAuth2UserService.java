package com.study.studyproject.global.oauth;

import com.study.studyproject.global.auth.UserDetailsImpl;
import com.study.studyproject.member.domain.Member;
import com.study.studyproject.member.domain.SocialType;
import com.study.studyproject.member.service.MemberService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

import java.util.Map;

import static com.study.studyproject.global.oauth.OAuthAttributes.of;
import static com.study.studyproject.member.domain.SocialType.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class CustomOAuth2UserService extends DefaultOAuth2UserService {
    private final MemberService memberService;

    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        log.info("OAuth2 로그인 시작");
        OAuth2User oAuth2User = super.loadUser(userRequest);

        String registrationId = getRegistrationId(userRequest);
        SocialType socialType = fromRegistrationId(registrationId);
        String userNameAttributeName = getUserNameAttributeName(userRequest);

        Map<String, Object> attributes = oAuth2User.getAttributes();

        OAuthAttributes extractAttributes = of(socialType, userNameAttributeName, attributes);
        Member member = memberService.getOrCreateUser(extractAttributes, socialType);

        return new UserDetailsImpl(member, member.getId(), member.getRole());
    }

    private static String getRegistrationId(OAuth2UserRequest userRequest) {
        return userRequest.getClientRegistration().getRegistrationId();
    }

    private static String getUserNameAttributeName(OAuth2UserRequest userRequest) {
        return userRequest.getClientRegistration()
                .getProviderDetails().getUserInfoEndpoint().getUserNameAttributeName();
    }


}
