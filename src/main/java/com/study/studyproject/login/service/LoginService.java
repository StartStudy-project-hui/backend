package com.study.studyproject.login.service;

import com.study.studyproject.global.exception.ex.DuplicateException;
import com.study.studyproject.global.exception.ex.TokenNotValidationException;
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
import com.study.studyproject.login.repository.RefreshRepository;
import com.study.studyproject.member.repository.MemberRepository;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.transaction.Transactional;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import static com.study.studyproject.global.exception.ex.ErrorCode.*;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class LoginService {

    private final RefreshRepository refreshRepository;
    private final MemberRepository memberRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    public static final String ACCESS_TOKEN = "Access_Token";


    public LoginResponseDto loginService(@Valid LoginRequest loginRequest, HttpServletResponse response) throws NotFoundException {

        Member member = memberRepository.findByEmail(loginRequest.getEmail().toString()).orElseThrow(() -> new NotFoundException(NOT_FOUND_MEMBER));

        if (!passwordEncoder.matches(loginRequest.getPwd(), member.getPassword())) {
            throw new NotFoundException(NOT_FOUND_PASSWORD);
        }

        TokenDtoResponse tokensDto = jwtUtil.createAllToken(loginRequest.getEmail(), member.getId());
        refreshRepository.findByAccessToken(tokensDto.getAccessToken())
                .ifPresentOrElse(
                        token -> refreshRepository.save(token.updateToken(tokensDto.getRefreshToken())), // 존재한다면
                        () -> refreshRepository.save(RefreshToken.toEntity(tokensDto, loginRequest)) //존재하지 않으면
                );


        jwtUtil.setHeader(response, tokensDto);

        return new LoginResponseDto("로그인 되었습니다.", HttpStatus.OK.value(), member.getNickname());

    }



    //회원가입
    public GlobalResultDto sign(@Valid SignRequest signRequest) {
        validate(signRequest);
        String encodePwd = passwordEncoder.encode(signRequest.getPwd());
        Member member = Member.toEntity(signRequest, encodePwd);
        memberRepository.save(member);
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
    }

    private boolean isPresentEmail(SignRequest signRequest) {
        return memberRepository.findByEmail(signRequest.getEmail()).isPresent();
    }

    //토큰 엑세스 토큰 재발급
    public String renewalAccessToken(String accessTokenRequest, String refreshTokenRequest, HttpServletResponse response ) {
        String accessToken = jwtUtil.resolveToken(accessTokenRequest);
        String refreshToken = jwtUtil.resolveToken(refreshTokenRequest);

        // 기존 AT와 RT일치하는지 확인
        if (jwtUtil.isValidRefreshAndInValidAccess(accessToken, refreshToken)) { //accessToken에 문제가 있는경우
            log.info("AccessToken 재발급 문제 ");

            Email emailFromToken = jwtUtil.getEmailFromToken(refreshToken);
            Long idFromToken = jwtUtil.getIdFromToken(refreshToken);

            //재발급
            RefreshToken getRefreshToken = refreshRepository.findById(emailFromToken).orElseThrow(() -> new TokenNotValidationException(INVALID_REFRESH_TOKEN));
            String renewAccessToken = jwtUtil.createToken(emailFromToken, idFromToken, ACCESS_TOKEN); //엑세스 토큰 재생성
            getRefreshToken.updateAccessToken(renewAccessToken); // 갱신
            jwtUtil.setHeader(response, TokenDtoResponse.of(renewAccessToken, refreshToken));
            return renewAccessToken;

        }

        //AT 문제가 둘다 없는 경우
        if (jwtUtil.isValidRefreshAndValidAccess(accessToken, refreshToken)) {
            log.info("둘다 문제 없는 경우");
            return accessToken;
        }


        //일치하지 않다면, 401
        throw new TokenNotValidationException(EXPIRED_PERIOD_TOKEN);

    }


}
