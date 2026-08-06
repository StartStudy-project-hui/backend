package com.study.studyproject.auth.service;

import com.study.studyproject.global.exception.ex.TokenNotValidationException;
import com.study.studyproject.global.jwt.JwtUtil;
import com.study.studyproject.auth.domain.RefreshToken;
import com.study.studyproject.auth.repository.RefreshRepository;
import com.study.studyproject.member.domain.Email;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static com.study.studyproject.global.jwt.JwtUtil.ACCESS_TOKEN;
import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private JwtUtil jwtUtil;

    @Mock
    private RefreshRepository refreshRepository;

    @Mock
    private HttpServletResponse response;

    @InjectMocks
    private LoginService loginService;

    private final String accessTokenRequest = "Bearer old-access-token";
    private final String refreshTokenRequest = "Bearer refresh-token";

    private final String accessToken = "old-access-token";
    private final String refreshToken = "refresh-token";

    private final String newAccessToken = "new-access-token";

    private final Email email = new Email("test@test.com");
    private final Long memberId = 1L;

    @Test
    @DisplayName("AccessToken과 RefreshToken이 모두 유효하면 기존 AccessToken을 반환한다")
    void renewalAccessToken_validAccessTokenAndValidRefreshToken_returnAccessToken() {
        // given
        when(jwtUtil.resolveToken(accessTokenRequest)).thenReturn(accessToken);
        when(jwtUtil.resolveToken(refreshTokenRequest)).thenReturn(refreshToken);

        when(jwtUtil.canUseExistingAccessToken(accessToken, refreshToken))
                .thenReturn(true);

        // when
        String result = loginService.renewalAccessToken(
                accessTokenRequest,
                refreshTokenRequest,
                response
        );

        // then
        assertThat(result).isEqualTo(accessToken);

        verify(jwtUtil).resolveToken(accessTokenRequest);
        verify(jwtUtil).resolveToken(refreshTokenRequest);
        verify(jwtUtil).canUseExistingAccessToken(accessToken, refreshToken);

        verify(jwtUtil, never()).needsAccessTokenReissue(anyString(), anyString());
        verify(refreshRepository, never()).findById(anyString());
    }

    @Test
    @DisplayName("AccessToken이 만료되고 RefreshToken이 유효하면 AccessToken을 재발급한다")
    void renewalAccessToken_invalidAccessTokenAndValidRefreshToken_reissueAccessToken() {
        // given
        RefreshToken savedRefreshToken = RefreshToken.builder()
                .email(email.address())
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .build();

        when(jwtUtil.resolveToken(accessTokenRequest)).thenReturn(accessToken);
        when(jwtUtil.resolveToken(refreshTokenRequest)).thenReturn(refreshToken);

        when(jwtUtil.canUseExistingAccessToken(accessToken, refreshToken))
                .thenReturn(false);

        when(jwtUtil.needsAccessTokenReissue(accessToken, refreshToken))
                .thenReturn(true);

        when(jwtUtil.getEmailFromToken(refreshToken)).thenReturn(email);
        when(jwtUtil.getIdFromToken(refreshToken)).thenReturn(memberId);

        when(refreshRepository.findById(email.address()))
                .thenReturn(Optional.of(savedRefreshToken));

        when(jwtUtil.createToken(email, memberId, ACCESS_TOKEN))
                .thenReturn(newAccessToken);

        // when
        String result = loginService.renewalAccessToken(
                accessTokenRequest,
                refreshTokenRequest,
                response
        );

        // then
        assertThat(result).isEqualTo(newAccessToken);
        assertThat(savedRefreshToken.getAccessToken()).isEqualTo(newAccessToken);

        verify(refreshRepository).findById(email.address());
        verify(jwtUtil).createToken(email, memberId, ACCESS_TOKEN);
    }

    @Test
    @DisplayName("AccessToken과 RefreshToken 조합이 유효하지 않으면 예외가 발생한다")
    void renewalAccessToken_invalidToken_throwException() {
        // given
        when(jwtUtil.resolveToken(accessTokenRequest)).thenReturn(accessToken);
        when(jwtUtil.resolveToken(refreshTokenRequest)).thenReturn(refreshToken);

        when(jwtUtil.canUseExistingAccessToken(accessToken, refreshToken))
                .thenReturn(false);

        when(jwtUtil.needsAccessTokenReissue(accessToken, refreshToken))
                .thenReturn(false);

        // when & then
        assertThatThrownBy(() -> loginService.renewalAccessToken(
                accessTokenRequest,
                refreshTokenRequest,
                response
        ))
                .isInstanceOf(TokenNotValidationException.class);

        verify(refreshRepository, never()).findById(anyString());
    }

    @Test
    @DisplayName("저장된 RefreshToken이 없으면 예외가 발생한다")
    void renewalAccessToken_refreshTokenNotFound_throwException() {
        // given
        when(jwtUtil.resolveToken(accessTokenRequest)).thenReturn(accessToken);
        when(jwtUtil.resolveToken(refreshTokenRequest)).thenReturn(refreshToken);

        when(jwtUtil.canUseExistingAccessToken(accessToken, refreshToken))
                .thenReturn(false);

        when(jwtUtil.needsAccessTokenReissue(accessToken, refreshToken))
                .thenReturn(true);

        when(jwtUtil.getEmailFromToken(refreshToken)).thenReturn(email);
        when(jwtUtil.getIdFromToken(refreshToken)).thenReturn(memberId);

        when(refreshRepository.findById(email.address()))
                .thenReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> loginService.renewalAccessToken(
                accessTokenRequest,
                refreshTokenRequest,
                response
        ))
                .isInstanceOf(TokenNotValidationException.class);
    }

    @Test
    @DisplayName("요청 RefreshToken과 저장된 RefreshToken이 다르면 예외가 발생한다")
    void renewalAccessToken_refreshTokenMismatch_throwException() {
        // given
        String savedTokenValue = "saved-refresh-token";

        RefreshToken savedRefreshToken = RefreshToken.builder()
                .email(email.address())
                .accessToken(accessToken)
                .refreshToken(savedTokenValue)
                .build();

        when(jwtUtil.resolveToken(accessTokenRequest)).thenReturn(accessToken);
        when(jwtUtil.resolveToken(refreshTokenRequest)).thenReturn(refreshToken);

        when(jwtUtil.canUseExistingAccessToken(accessToken, refreshToken))
                .thenReturn(false);

        when(jwtUtil.needsAccessTokenReissue(accessToken, refreshToken))
                .thenReturn(true);

        when(jwtUtil.getEmailFromToken(refreshToken)).thenReturn(email);
        when(jwtUtil.getIdFromToken(refreshToken)).thenReturn(memberId);

        when(refreshRepository.findById(email.address()))
                .thenReturn(Optional.of(savedRefreshToken));

        // when & then
        assertThatThrownBy(() -> loginService.renewalAccessToken(
                accessTokenRequest,
                refreshTokenRequest,
                response
        ))
                .isInstanceOf(TokenNotValidationException.class);

        verify(jwtUtil, never()).createToken(any(), anyLong(), any());
    }
}