package com.study.studyproject.global.jwt;

import static com.mysema.commons.lang.Assert.assertThat;
import static org.mockito.BDDMockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.study.studyproject.blacklist.domain.BlackList;
import com.study.studyproject.blacklist.repository.blacklist.BlackListRepository;
import com.study.studyproject.global.exception.ex.ErrorCode;
import com.study.studyproject.global.jwt.JwtFilter;
import com.study.studyproject.global.jwt.JwtUtil;
import com.study.studyproject.member.domain.Email;
import jakarta.servlet.FilterChain;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import java.util.Optional;

@ExtendWith(MockitoExtension.class)
class JwtFilterTest {

    @InjectMocks
    private JwtFilter jwtFilter;

    @Mock
    private JwtUtil jwtUtil;

    @Mock
    private BlackListRepository blackListRepository;

    @Mock
    private FilterChain filterChain;

    @Spy
    private ObjectMapper objectMapper = new ObjectMapper();
    private MockHttpServletRequest request;
    private MockHttpServletResponse response;

    @BeforeEach
    void setUp() {
        request = new MockHttpServletRequest();
        response = new MockHttpServletResponse();
    }

    @Test
    @DisplayName("토큰이 없는 비회원 요청은 그대로 통과해야 한다")
    void anonymousUserPass() throws Exception {
        // given
        given(jwtUtil.getHeaderToken(any(), any())).willReturn(null);
        given(jwtUtil.resolveToken(null)).willReturn(null);

        // when
        jwtFilter.doFilterInternal(request, response, filterChain);

        // then
        verify(filterChain).doFilter(request, response);
    }


    @Test
    @DisplayName("만료된 토큰(isNotInvalidToken)인 경우 401 에러를 반환해야 한다")
    void invalidTokenResponse() throws Exception {
        // given
        String token = "expired.token.here";
        given(jwtUtil.getHeaderToken(any(), any())).willReturn("Bearer " + token);
        given(jwtUtil.resolveToken(any())).willReturn(token);
        given(jwtUtil.AccessTokenValidation(token)).willReturn(false); // 유효하지 않음

        // when
        jwtFilter.doFilterInternal(request, response, filterChain);

        // then
        Assertions.assertEquals(401,response.getStatus());
        verify(filterChain, never()).doFilter(request, response);
    }

    @Test
    @DisplayName("블랙리스트 유저인 경우 403 에러를 반환해야 한다")
    void blacklistedUserResponse() throws Exception {
        // given
        String token = "valid.token.here";
        String email = "test@test.com";
        BlackList mockBlackList = mock(BlackList.class);

        given(jwtUtil.resolveToken(any())).willReturn(token);
        given(jwtUtil.AccessTokenValidation(token)).willReturn(true); // 토큰은 유효함
        given(jwtUtil.getEmailFromToken(token)).willReturn(new Email(email));
        given(mockBlackList.isBlocked()).willReturn(true);

        // 블랙리스트에 존재한다고 가정
        given(blackListRepository.findByHashValue(any()))
                .willReturn(Optional.of(mockBlackList));
        // when
        jwtFilter.doFilterInternal(request, response, filterChain);

        // then
        Assertions.assertEquals(403,response.getStatus());

    }


}