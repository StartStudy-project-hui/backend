package com.study.studyproject.global.config;

import com.study.studyproject.global.auth.UserDetailsImpl;
import com.study.studyproject.global.jwt.JwtAccessDeniedHandler;
import com.study.studyproject.global.jwt.JwtAuthenticationEntryPoint;
import com.study.studyproject.global.jwt.JwtFilter;
import com.study.studyproject.global.jwt.JwtUtil;
import com.study.studyproject.global.oauth.CustomOAuth2UserService;
import com.study.studyproject.global.oauth.handler.OAuth2LoginFailureHandler;
import com.study.studyproject.global.oauth.handler.OAuth2LoginSuccessHandler;
import com.study.studyproject.login.repository.RefreshRepository;
import com.study.studyproject.member.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityCustomizer;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.annotation.web.configurers.LogoutConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.AnonymousAuthenticationFilter;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.authentication.logout.LogoutFilter;
import org.springframework.web.filter.CorsFilter;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
@Slf4j
public class SpringSecurity {

    private final JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint;
    private final JwtAccessDeniedHandler jwtAccessDeniedHandler;
    private final CustomOAuth2UserService customOAuth2UserService;
    private final MemberRepository memberRepository;
    private final RefreshRepository refreshRepository;
    private final CorsFilter filter;
    private final JwtUtil jwtUtil;


    @Bean
    public BCryptPasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    //제외될 url
    @Bean
    public WebSecurityCustomizer webSecurityCustomizer() {
        return web -> web.ignoring().requestMatchers("/h2-console/**","/actuator/**"); //제외될 url
    }

    @Bean
    public JwtFilter jwtFilter() {
        return new JwtFilter(jwtUtil); // JwtFilter를 빈으로 등록
    }


    /**
     * 로그인 성공 시 호출되는 LoginSuccessJWTProviderHandler 빈 등록
     */
    @Bean
    public OAuth2LoginSuccessHandler loginSuccessHandler() {
        return new OAuth2LoginSuccessHandler(jwtUtil, memberRepository, refreshRepository);
    }

    /**
     * 로그인 실패 시 호출되는 LoginFailureHandler 빈 등록
     */
    @Bean
    public OAuth2LoginFailureHandler loginFailureHandler() {
        return new OAuth2LoginFailureHandler();
    }


    @Bean
    public AnonymousAuthenticationFilter anonymousAuthenticationFilter() {
        return new AnonymousAuthenticationFilter("anonymousKey", "anonymousUser",
                AuthorityUtils.createAuthorityList("ROLE_GUEST"));
    }



    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http.addFilter(filter);
        http.csrf(AbstractHttpConfigurer::disable) //
                .sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS)) // 세션 생성x
                .formLogin(f -> f.disable())
                .httpBasic(h -> h.disable())
                .exceptionHandling(exceptionHandling -> exceptionHandling
                        .accessDeniedHandler(jwtAccessDeniedHandler)
                        .authenticationEntryPoint(jwtAuthenticationEntryPoint)
                );

        http.oauth2Login(
                oauth2 -> oauth2.userInfoEndpoint(userInfoEndpointConfig ->
                                userInfoEndpointConfig.userService(customOAuth2UserService))
                        .successHandler(loginSuccessHandler())
                        .failureHandler(loginFailureHandler()));


        http.authorizeHttpRequests(authorize ->
                authorize
                        .requestMatchers("/api/v*/reply/view/**", "/api/v*/", "/api/renew-token"
                                , "/api/v*/auth/**", "/api/v*/board/**", "/api/v*/view/post-like/**").permitAll()
                        .requestMatchers("api/v*/board/member/**", "/api/v*/user/**", "/api/v*/postLike/**", "/api/v*/reply/**").authenticated()
                        .requestMatchers("api/v*/admin/**").hasRole("ADMIN")
                        .requestMatchers("/actuator/**").permitAll()
                        .anyRequest().permitAll()
        );




        http.addFilterBefore(jwtFilter(), UsernamePasswordAuthenticationFilter.class);


        return http.build();

    }


}
