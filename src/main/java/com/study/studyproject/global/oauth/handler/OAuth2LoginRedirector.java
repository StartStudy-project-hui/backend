package com.study.studyproject.global.oauth.handler;

import jakarta.servlet.http.HttpServletResponse;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.IOException;

final class OAuth2LoginRedirector {

    static final String LOGIN_SUCCESS_PARAM = "loginSuccess";

    private OAuth2LoginRedirector() {
    }

    static void redirect(HttpServletResponse response, String location, boolean loginSuccess) throws IOException {
        String redirectionUri = UriComponentsBuilder.fromUriString(location)
                .queryParam(LOGIN_SUCCESS_PARAM, loginSuccess)
                .build()
                .toUriString();
        response.sendRedirect(redirectionUri);
    }

}
