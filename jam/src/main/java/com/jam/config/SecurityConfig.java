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
import com.jam.global.security.CustomUserDetailsService;
import com.jam.global.security.JwtAuthenticationFilter;

import lombok.RequiredArgsConstructor;

@Configuration
@EnableWebSecurity()
@EnableGlobalMethodSecurity(prePostEnabled = true)
@RequiredArgsConstructor
@ComponentScan(basePackages = "com.jam")
public class SecurityConfig extends WebSecurityConfigurerAdapter {

	private final CustomLoginSuccessHandler customSuccessHandler;
	private final CustomLoginFailureHandler customFailureHandler;
	private final CustomLogoutHandler customLogoutHandler;
	private final PasswordEncoder passwordEncoder;
	private final CustomLogoutSuccessHandler customLogoutSuccessHanler;
	private final CustomUserDetailsService customUserDetailsService;
	private final CustomAuthEntryPoint customAuthEntryPoint;
	private final CustomAccessDeniedHandler customAccessDeniedHandler;
	private final JwtAuthenticationFilter jwtAuthenticationFilter;
	
	@Bean
	@Override
	public AuthenticationManager authenticationManagerBean() throws Exception {
		return super.authenticationManagerBean();
	}
	
	private static final String[] AUTH_WHITELIST = {
		    "/api/member/auth/check", "/api/member/me/token", "/api/member/logout",
		    "/oauth/kakao/logout", "/oauth/naver/logout", "/api/member/userName",
		    "/api/member/phone", "/api/member/verify-password", "/api/member/password",
		    "/api/member/address", "/api/member/deleteAccount", "/api/member/convertBusiness",
		    "/api/chat/**", "/api/mypage/myPosts", "/api/mypage/account",
		    "/api/files/**/download-url"
		};
	
	@Override
	protected void configure(HttpSecurity http) throws Exception {

		http
			.csrf().disable()
			.exceptionHandling()
				.authenticationEntryPoint(customAuthEntryPoint)
				.accessDeniedHandler(customAccessDeniedHandler)
			.and()
			.httpBasic()
				.disable()
			.authorizeRequests()
				// .antMatchers("/admin/**").access("hasRole('ADMIN')")
				// .antMatchers("/admin/admin").hasRole("ADMIN")
				.antMatchers("/.well-known/**")
				.denyAll()
				.antMatchers(AUTH_WHITELIST).authenticated()
				.anyRequest().permitAll()
			.and()
			.formLogin()
				.loginPage("/member/login")
				.loginProcessingUrl("/api/member/login-process")
				.successHandler(customSuccessHandler)
				.failureHandler(customFailureHandler)
			.and()
			.logout()
				.logoutRequestMatcher(new AntPathRequestMatcher("/api/member/logout", "POST")) 
				.invalidateHttpSession(true)
				.addLogoutHandler(customLogoutHandler)
				.logoutSuccessHandler(customLogoutSuccessHanler)
			.and()
			.sessionManagement()
				.sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED)
			// .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
			.and()
				.addFilterBefore(jwtAuthenticationFilter,
					UsernamePasswordAuthenticationFilter.class);
	}

	@Override
	protected void configure(AuthenticationManagerBuilder auth) throws Exception {
		auth.userDetailsService(customUserDetailsService).passwordEncoder(passwordEncoder);
	}
}