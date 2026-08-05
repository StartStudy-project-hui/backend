package com.study.studyproject.login.service;

import com.study.studyproject.global.exception.ex.BadRequestException;
import com.study.studyproject.global.exception.ex.DuplicateException;
import com.study.studyproject.global.exception.ex.TokenNotValidationException;
import com.study.studyproject.login.domain.PasswordEncoder;
import com.study.studyproject.member.domain.Email;
import com.study.studyproject.member.domain.Member;
import com.study.studyproject.login.domain.RefreshToken;
import com.study.studyproject.global.GlobalResultDto;
import com.study.studyproject.global.exception.ex.NotFoundException;
import com.study.studyproject.global.jwt.JwtUtil;
import com.study.studyproject.login.dto.LoginRequest;
import com.study.studyproject.login.dto.LoginResponseDto;
import com.study.studyproject.login.dto.SignRequest;
import com.study.studyproject.login.dto.TokenDtoResponse;
import com.study.studyproject.login.email.service.EmailVerificationService;
import com.study.studyproject.login.repository.RefreshRepository;
import com.study.studyproject.member.repository.MemberRepository;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.transaction.Transactional;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import static com.study.studyproject.global.exception.ex.ErrorCode.*;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class LoginService {

    private final RefreshRepository refreshRepository;
    private final RefreshTokenService refreshTokenService;
    private final MemberRepository memberRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final EmailVerificationService emailVerificationService;
    public static final String ACCESS_TOKEN = "Access_Token";


    public LoginResponseDto loginService(@Valid LoginRequest loginRequest, HttpServletResponse response) throws NotFoundException {

        Email email = new Email(loginRequest.getEmail());
        Member member = memberRepository.findByEmail(email).orElseThrow(() -> new NotFoundException(NOT_FOUND_MEMBER));
        verifyPassword(loginRequest, member);

        TokenDtoResponse tokensDto = saveOrUpdateRefreshToken(email, member);
        setTokenHeader(response, tokensDto);

        return new LoginResponseDto("로그인 되었습니다.", HttpStatus.OK.value(), member.getNickname());

    }

    private void setTokenHeader(HttpServletResponse response, TokenDtoResponse tokensDto) {
        jwtUtil.setHeader(response, tokensDto);
    }

    private TokenDtoResponse saveOrUpdateRefreshToken(Email email, Member member) {
        TokenDtoResponse tokensDto = jwtUtil.createAllToken(email, member.getId());
        refreshTokenService.saveOrUpdate(email.address(), tokensDto);
        return tokensDto;
    }

    private void verifyPassword(LoginRequest loginRequest, Member member) {
        if (!member.verifyPassword(loginRequest.getPwd(), passwordEncoder)) {
            throw new NotFoundException(NOT_FOUND_PASSWORD);
        }
    }


    //회원가입
    public GlobalResultDto sign(@Valid SignRequest signRequest) {
        validate(signRequest);

        Member member = Member.toEntity(signRequest, passwordEncoder);
        memberRepository.save(member);
        emailVerificationService.clearVerification(signRequest.getEmail());
        return new GlobalResultDto("회원가입 성공", HttpStatus.OK.value());

    }

    private void validate(SignRequest signRequest) {
        if (signRequest.isNotEqualsCheckPwd()) {
            throw new NotFoundException(NOT_FOUND_PASSWORD);
        }
        //중복 확인
        if (isPresentEmail(signRequest)) {
            throw new DuplicateException(MEMBER_DUPLICATED);
        }
        //이메일 인증 확인
        if (!emailVerificationService.isVerified(signRequest.getEmail())) {
            throw new BadRequestException(EMAIL_NOT_VERIFIED);
        }
    }

    private boolean isPresentEmail(SignRequest signRequest) {
        return memberRepository.findByEmail(new Email(signRequest.getEmail())).isPresent();
    }

    //토큰 엑세스 토큰 재발급
    public String renewalAccessToken(String accessTokenRequest, String refreshTokenRequest, HttpServletResponse response ) {
        String accessToken = jwtUtil.resolveToken(accessTokenRequest);
        String refreshToken = jwtUtil.resolveToken(refreshTokenRequest);


        //AT 문제가 둘다 없는 경우
        if (jwtUtil.canUseExistingAccessToken(accessToken, refreshToken)) {
            log.info("둘다 문제 없는 경우");
            return accessToken;
        }


        if (jwtUtil.needsAccessTokenReissue(accessToken, refreshToken)) {
            log.info("AccessToken 만료, RefreshToken 정상 - AccessToken 재발급");
            return reissueAccessToken(refreshToken, response);
        }


        //일치하지 않다면, 401
        throw new TokenNotValidationException(EXPIRED_PERIOD_TOKEN);

    }
    private String reissueAccessToken(
            String refreshToken,
            HttpServletResponse response
    ) {
        Email emailFromToken = jwtUtil.getEmailFromToken(refreshToken);
        Long idFromToken = jwtUtil.getIdFromToken(refreshToken);

        //재발급
        RefreshToken savedRefreshToken = findSavedRefreshToken(emailFromToken);

        validateRefreshTokenMatch(savedRefreshToken, refreshToken);
        String renewAccessToken = jwtUtil.createToken(emailFromToken, idFromToken, ACCESS_TOKEN); //엑세스 토큰 재생성
        refreshRepository.save(savedRefreshToken.updateAccessToken(renewAccessToken)); // 갱신 후 Redis에 반영
        setTokenHeader(response, TokenDtoResponse.of(renewAccessToken, refreshToken));
        return renewAccessToken;
    }

    private RefreshToken findSavedRefreshToken(Email emailFromToken) {
        RefreshToken getRefreshToken = refreshRepository.findById(emailFromToken.address()).orElseThrow(() -> new TokenNotValidationException(INVALID_REFRESH_TOKEN));
        return getRefreshToken;
    }

    private void validateRefreshTokenMatch(
            RefreshToken savedRefreshToken,
            String requestRefreshToken
    ) {
        if (!savedRefreshToken.getRefreshToken().equals(requestRefreshToken)) {
            throw new TokenNotValidationException(INVALID_REFRESH_TOKEN);
        }
    }


}
