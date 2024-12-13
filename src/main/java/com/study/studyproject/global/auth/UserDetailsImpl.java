package com.study.studyproject.global.auth;

import com.study.studyproject.member.domain.Member;
import com.study.studyproject.login.domain.Role;
import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.oauth2.core.user.OAuth2User;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;

@Getter
public class UserDetailsImpl implements UserDetails, OAuth2User {


    private Role authority;
    private Long memberId;
    private  Member member;
    private Map<String, Object> attributes;
    private String email;


    public UserDetailsImpl(Member member, Role authority, Long memberId) {
        this.member = member;
        this.authority = authority;
        this.memberId = memberId;
    }

    //oAuth 로그인
    public UserDetailsImpl(Member user, Long memberId, Role authority, String email) {
        this.member = user;
        this.memberId = memberId;
        this.authority = authority;
        this.email = email;
    }


    public Map<String, Object> getAttributes() {
        return null;
    }


    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        Collection<GrantedAuthority> collection = new ArrayList<>();

        collection.add(new GrantedAuthority() {
            @Override
            public String getAuthority() { //권한주는
                return authority.name();
            }

        })
        ;

        return collection;
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

    public String getEmail() {
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
