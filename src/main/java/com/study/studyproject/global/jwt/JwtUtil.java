package com.study.studyproject.global.jwt;

import com.study.studyproject.global.auth.UserDetailsServiceImpl;
import com.study.studyproject.global.exception.ex.TokenNotValidationException;
import com.study.studyproject.login.dto.TokenDtoResponse;
import com.study.studyproject.member.domain.Email;
import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseCookie;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.security.Key;
import java.util.*;

import static com.study.studyproject.global.exception.ex.ErrorCode.EXPIRED_PERIOD_TOKEN;
import static com.study.studyproject.global.exception.ex.ErrorCode.INVALID_REFRESH_TOKEN;

@Slf4j
@Component
@RequiredArgsConstructor
public class JwtUtil {

    public static final String BEARER = "Bearer ";
    public static final String ID = "id";
    private final UserDetailsServiceImpl userDetailsService;
    private static final long ACCESS_TIME = 30 * 60 * 1000L;
    private static final long REFRESH_TIME =  24 * 60 * 60 * 1000L;
    public static final String ACCESS_TOKEN = "Access_Token";
    public static final String REFRESH_TOKEN = "Refresh_Token";



    @Value("${jwt.secret}")
    private String secretKey;
    private Key key;
    private final SignatureAlgorithm signatureAlgorithm = SignatureAlgorithm.HS256;

    // bean으로 등록 되면서 딱 한번 실행이 됩니다.
    @PostConstruct
    public void init() {
        byte[] bytes = Base64.getDecoder().decode(secretKey);
        key = Keys.hmacShaKeyFor(bytes);
    }

    // header 토큰을 가져오는 기능
    public String getHeaderToken(HttpServletRequest request, String type) {
        return type.equals(ACCESS_TOKEN) ? request.getHeader(ACCESS_TOKEN) : request.getHeader(REFRESH_TOKEN);
    }


     public void setHeader(HttpServletResponse response, TokenDtoResponse tokensDto) {
        response.addHeader(JwtUtil.ACCESS_TOKEN, JwtUtil.BEARER + tokensDto.getAccessToken());
        response.addHeader(JwtUtil.REFRESH_TOKEN, JwtUtil.BEARER + tokensDto.getRefreshToken());
    }

    public void setCookie(HttpServletResponse response, TokenDtoResponse tokensDto) {
        ResponseCookie accessTokenCookie = ResponseCookie.from(ACCESS_TOKEN, tokensDto.getAccessToken())
                .path("/")
                .httpOnly(false)
                .secure(true)
                .sameSite("None")
                .build();

        ResponseCookie refreshTokenCookie = ResponseCookie.from(REFRESH_TOKEN, tokensDto.getRefreshToken())
                .path("/")
                .httpOnly(false)
                .secure(true)
                .sameSite("None")
                .build();
        response.addHeader("Set-Cookie", accessTokenCookie.toString());
        response.addHeader("Set-Cookie", refreshTokenCookie.toString());


    }


    // 토큰 생성
    public TokenDtoResponse createAllToken(Email email, Long id) {
        String access = createToken(email, id, ACCESS_TOKEN);
        String refresh = createToken(email, id, REFRESH_TOKEN);
        return new TokenDtoResponse(access, refresh);
    }

    public String createToken(Email email,Long id, String type) {

        Date date = new Date();

        long time = type.equals(ACCESS_TOKEN) ? ACCESS_TIME : REFRESH_TIME;

        return Jwts.builder()
                .setSubject(email.address())
                .claim(ID, id)
                .setExpiration(new Date(date.getTime() + time))
                .setIssuedAt(date)
                .signWith(key, signatureAlgorithm)
                .compact();
    }



    // 토큰 검증
    public Boolean isAccessTokenValid(String token) throws ExpiredJwtException, TokenNotValidationException {
        try {
            Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(token);
            return true;
        } catch (io.jsonwebtoken.security.SecurityException | MalformedJwtException e) {
            log.error("잘못된 JWT 서명입니다.", e);
        } catch (ExpiredJwtException e) {
            log.error("만료된 JWT 토큰입니다.", e);
        } catch (UnsupportedJwtException e) {
            log.error("지원되지 않는 JWT 토큰입니다.", e);
        } catch (IllegalArgumentException e) {
            log.error("잘못된 형식의 JWT 토큰입니다.", e);
        }
        return false;
    }


    private void validateRefreshToken(final String refreshToken) throws TokenNotValidationException {
        try {
            Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(refreshToken);
        } catch (final ExpiredJwtException | SecurityException | MalformedJwtException | UnsupportedJwtException e) {
            throw new TokenNotValidationException(EXPIRED_PERIOD_TOKEN);
        } catch (final JwtException | IllegalArgumentException e) {
            throw new TokenNotValidationException(INVALID_REFRESH_TOKEN);
        }
    }

    // 리프레시 토큰은 유효하지만 액세스 토큰은 만료되어 재발급이 필요한 경우
    public boolean needsAccessTokenReissue(String accessToken, String refreshToken) {
        validateRefreshToken(refreshToken);
        return !isAccessTokenValid(accessToken);
    }



    // 인증 객체 생성
    public Authentication createAuthentication(String email) {
        UserDetails userDetails = userDetailsService.loadUserByUsername(email);
        return new UsernamePasswordAuthenticationToken(userDetails, "", userDetails.getAuthorities());
    }

    // 토큰에서 email 가져오는 기능
    public Email getEmailFromToken(String token) {
        String subject = Jwts.parserBuilder()
                .setSigningKey(key)
                .build()
                .parseClaimsJws(token)
                .getBody()
                .getSubject();

        return new Email(subject);
    }
    // 토큰에서 id 가져오는 기능
    public Long getIdFromToken(String token) {
        return Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(token).getBody().get(ID,Long.class);
    }


    public  String resolveToken(String token) {

        if (StringUtils.hasText(token) && token.startsWith(BEARER)) {
            String[] split = token.split(" ");
            return split[1];
        }

        return null;
    }


    // 리프레시, 액세스 토큰 모두 유효하여 기존 액세스 토큰을 그대로 쓸 수 있는 경우
    public boolean canUseExistingAccessToken(String accessToken, String refreshToken) {
        try {
            validateRefreshToken(refreshToken);
        } catch (final TokenNotValidationException e) {
            return false;
        }
        return isAccessTokenValid(accessToken);
    }
}
