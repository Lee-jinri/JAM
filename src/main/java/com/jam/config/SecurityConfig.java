package com.jam.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;

import com.jam.global.handler.CustomAccessDeniedHandler;
import com.jam.global.handler.CustomAuthEntryPoint;
import com.jam.global.handler.CustomLoginFailureHandler;
import com.jam.global.handler.CustomLoginSuccessHandler;
import com.jam.global.handler.CustomLogoutHandler;
import com.jam.global.handler.CustomLogoutSuccessHandler;
import com.jam.global.security.CustomUserDetailsService;
import com.jam.global.security.JwtAuthenticationFilter;

import lombok.RequiredArgsConstructor;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {
	private final CustomLoginSuccessHandler customSuccessHandler;
    private final CustomLoginFailureHandler customFailureHandler;
    private final CustomLogoutHandler customLogoutHandler;
    private final CustomLogoutSuccessHandler customLogoutSuccessHandler;
    private final CustomUserDetailsService customUserDetailsService;
    private final CustomAuthEntryPoint customAuthEntryPoint;
    private final CustomAccessDeniedHandler customAccessDeniedHandler;
    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    
    private static final String[] AUTH_REQUIRED_LIST = {
            "/api/member/auth/check", "/api/member/me/token", "/api/member/logout",
            "/oauth/kakao/logout", "/oauth/naver/logout", "/api/member/userName",
            "/api/member/phone", "/api/member/verify-password", "/api/member/password",
            "/api/member/address", "/api/member/deleteAccount", "/api/member/convertBusiness",
            "/api/chat/**", "/api/mypage/myPosts", "/api/mypage/account",
            "/api/files/*/download-url"
        };
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
        	.csrf(csrf -> csrf.disable())
            .exceptionHandling(conf -> conf
                .authenticationEntryPoint(customAuthEntryPoint)
                .accessDeniedHandler(customAccessDeniedHandler)
            )
            .authorizeHttpRequests(auth -> auth
                // 로그인 필수
                .requestMatchers(AUTH_REQUIRED_LIST).authenticated()
                // .requestMatchers("/admin/**").hasRole("ADMIN")
                .requestMatchers("/.well-known/**").denyAll()
                .anyRequest().permitAll()
            )
            .formLogin(form -> form
                .loginPage("/member/login")
                .loginProcessingUrl("/api/member/login-process")
                .successHandler(customSuccessHandler)
                .failureHandler(customFailureHandler)
                .permitAll()
            )
            .logout(logout -> logout
                .logoutRequestMatcher(new AntPathRequestMatcher("/api/member/logout", "POST"))
                .addLogoutHandler(customLogoutHandler)
                .logoutSuccessHandler(customLogoutSuccessHandler)
                .invalidateHttpSession(true)
            )
            .sessionManagement(session -> session
                .sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED)
            )
            // JWT 필터 추가
            .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    // 예전 configure(AuthenticationManagerBuilder auth) 역할을 스프링이 알아서 하도록 놔두면 되지만,
    // 명시적으로 설정하고 싶다면 아래와 같이 작성할 수 있습니다. (생략 가능)
    /*
    @Bean
    public DaoAuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();
        authProvider.setUserDetailsService(customUserDetailsService);
        authProvider.setPasswordEncoder(passwordEncoder);
        return authProvider;
    }
    */
}
