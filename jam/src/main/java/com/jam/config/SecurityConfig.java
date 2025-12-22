package com.jam.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;

import com.jam.global.handler.CustomAccessDeniedHandler;
import com.jam.global.handler.CustomAuthEntryPoint;
import com.jam.global.handler.CustomLoginFailureHandler;
import com.jam.global.handler.CustomLoginSuccessHandler;
import com.jam.global.handler.CustomLogoutHandler;
import com.jam.global.handler.CustomLogoutSuccessHandler;
import com.jam.global.jwt.JwtService;
import com.jam.global.security.CustomUserDetailsService;
import com.jam.global.security.JwtAuthenticationFilter;

import lombok.RequiredArgsConstructor;

@Configuration
@EnableWebSecurity()
@EnableGlobalMethodSecurity(prePostEnabled = true)
@RequiredArgsConstructor
@ComponentScan(basePackages = "com.jam")
public class SecurityConfig extends WebSecurityConfigurerAdapter {

	private final JwtService jwtService;
	private final CustomLoginSuccessHandler customSuccessHandler;
	private final CustomLoginFailureHandler customFailureHandler;
	private final CustomLogoutHandler customLogoutHandler;
	private final PasswordEncoder passwordEncoder;
	private final CustomLogoutSuccessHandler customLogoutSuccessHanler;
	private final CustomUserDetailsService customUserDetailsService;
	private final CustomAuthEntryPoint customAuthEntryPoint;
	private final CustomAccessDeniedHandler customAccessDeniedHandler;
	private final JwtAuthenticationFilter jwtAuthenticationFilter;
	
	/*
	@Bean
	public UserDetailsService customUserService() {
		return new CustomUserDetailsService(null);
	}*/

	@Bean
	@Override
	public AuthenticationManager authenticationManagerBean() throws Exception {
		return super.authenticationManagerBean();
	}

	/*
	 * Spring Framework에서 CORS (Cross-Origin Resource Sharing) 설정을 정의
	 * addAllowedOrigin : 허용할 출처를 입력(프론트엔드의 도메인과 포트를 입력.) addAllowedHeader : 허용할 헤더를
	 * 입력 addAllowedMethod : 허용할 Http Method를 입력 setAllowCredentials : 쿠키 요청을 허용하도록
	 * true로 설정
	 
	@Bean
	public CorsConfigurationSource corsConfigurationSource() {
		CorsConfiguration config = new CorsConfiguration();

		config.setAllowCredentials(true);
		config.setAllowedOrigins(Arrays.asList("http://localhost:8080"));
		config.setAllowedMethods(Arrays.asList("HEAD", "POST", "GET", "DELETE", "PUT"));
		config.setAllowedHeaders(Arrays.asList("*"));

		UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
		source.registerCorsConfiguration("/**", config); // 모든 엔드포인트에 CORS 설정을 적용

		return source;
	}*/

	@Override
	protected void configure(HttpSecurity http) throws Exception {

		http
			.exceptionHandling()
				.authenticationEntryPoint(customAuthEntryPoint)
				.accessDeniedHandler(customAccessDeniedHandler)
			.and()
			.httpBasic()
				.disable()
			/*.cors()
				.configurationSource(corsConfigurationSource())
			.and()*/
			.authorizeRequests()
				// .antMatchers("/admin/**").access("hasRole('ADMIN')")
				// .antMatchers("/admin/admin").hasRole("ADMIN")
				.antMatchers("/.well-known/**")
				.denyAll() // 차단
				.antMatchers(
						"/api/member/auth/check",
        				"/api/member/me/token", 
        				"/api/member/logout", 
        				"/oauth/kakao/logout",
        				"/oauth/naver/logout",
        				"/api/member/userName",
        				"/api/member/phone",
        				"/api/member/verify-password",
        				"/api/member/password",
        				"/api/member/address",
        				"/api/member/deleteAccount",
        				"/api/member/convertBusiness",
        				
        				"/api/chat/**",
        				
        				"/api/mypage/myPosts",
        				"/api/mypage/account",
        				
        				"/api/files/**/download-url"
					).authenticated()
			.and()
			.formLogin()
				.loginPage("/member/login")
				.loginProcessingUrl("/api/member/login-process")
				.successHandler(customSuccessHandler)
				.failureHandler(customFailureHandler)
			.and()
			.logout()
				.logoutRequestMatcher(new AntPathRequestMatcher("/api/member/logout", "POST")) // POST 방식으로 로그아웃 처리
				.invalidateHttpSession(true)
				.deleteCookies("remember-me", "JSESSIONID")
				.deleteCookies("JSESSIONID")
				.addLogoutHandler(customLogoutHandler)
				.logoutSuccessHandler(customLogoutSuccessHanler)
			.and()
			.sessionManagement()
				.sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED)
			// .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
			.and()
			/* UsernamePasswordAuthenticationFilter: 로그인 처리
			 * JwtAuthenticationFilter: 로그인 이후의 모든 요청을 처리하는 필터
			 */
				.addFilterBefore(jwtAuthenticationFilter,
					UsernamePasswordAuthenticationFilter.class)
			.csrf().disable();
	}

	/* customUserService 인증 되면 -> 스프링 시큐리티가 Authentication 객체를 자동으로 생성 -> */
	@Override
	protected void configure(AuthenticationManagerBuilder auth) throws Exception {
		//auth.userDetailsService(customUserService()).passwordEncoder(passwordEncoder);
		auth.userDetailsService(customUserDetailsService).passwordEncoder(passwordEncoder);
	}

}