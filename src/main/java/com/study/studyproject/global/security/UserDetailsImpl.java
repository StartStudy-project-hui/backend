package com.study.studyproject.global.security;

import com.study.studyproject.member.domain.Email;
import com.study.studyproject.member.domain.Member;
import com.study.studyproject.auth.domain.Role;
import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.oauth2.core.user.OAuth2User;

import java.util.Collection;
import java.util.List;
import java.util.Map;

@Getter
public class UserDetailsImpl implements UserDetails, OAuth2User {


    private Role authority;
    private Long memberId;
    private Member member;

    public UserDetailsImpl(Member member, Long memberId, Role authority) {
        this.member = member;
        this.memberId = memberId;
        this.authority = authority;
    }


    public Map<String, Object> getAttributes() {
        return null;
    }


    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority(authority.name()));
    }

    @Override
    public String getPassword() {
        return member != null ? member.getPassword():null;
    }

    @Override
    public String getUsername() {
        return member != null ? member.getUsername():null;
    }

    public String getNickname() {
        return member != null ? member.getNickname():null;
    }

    public Email getEmail() {
        return member != null ? member.getEmail():null;
    }


    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }

    @Override
    public String getName() {
        return null;
    }
}
