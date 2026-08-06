package com.study.studyproject.auth.domain;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

@Getter
@RequiredArgsConstructor
public enum Role {
    ROLE_USER,
    ROLE_ADMIN;

    public static boolean isAdmin(Role role) {
        return role == Role.ROLE_ADMIN;
    }

    public static boolean isAnonymous() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || authentication instanceof AnonymousAuthenticationToken) {
            return true;
        }

        return false;
    }




}
