package com.jam.config;

import javax.sql.DataSource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.builders.WebSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;

import com.jam.security.CustomLoginSuccessHandler;
import com.jam.security.CustomUserDetailsService;

import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.extern.log4j.Log4j;

@Configuration
@EnableWebSecurity(debug = true)
//@RequiredArgsConstructor
@Log4j
public class SecurityConfig extends WebSecurityConfigurerAdapter  {

	@Setter
	private DataSource dataSource;
	
	@Bean
	public AuthenticationSuccessHandler loginSuccessHandler() {
		return new CustomLoginSuccessHandler();
	}

    @Bean
    public PasswordEncoder passwordEncoder(){
        return new BCryptPasswordEncoder();
    }
    
    
    @Bean
    public UserDetailsService customUserService() {
    	return new CustomUserDetailsService();
    }
    
    
    
	@Override
    protected void configure(HttpSecurity http) throws Exception{
        
		http
			.authorizeRequests()
				.antMatchers("/admin/**").access("hasRole('ADMIN')")
				//.antMatchers("/admin/admin").hasRole("ROLE_ADMIN")
				.antMatchers("/*").permitAll()
			.and()
				.formLogin().loginPage("/member/login") // 커스텀 로그인 페이지 URL
				.permitAll() // 로그인 페이지는 모든 사용자에게 허용
				.loginProcessingUrl("/member/login")
				.successHandler(loginSuccessHandler())
				.usernameParameter("user_id") 
	            .passwordParameter("user_pw") 
				//.failureUrl("/member/login?error")
            .and()
            	.logout() // 로그아웃 설정
            	.logoutUrl("/member/logout") // 로그아웃 URL
            	//.invalidateHttpSession(true)
            	//.deleteCookies("remember-me","JSESSTION_ID")
            .and()
            	.csrf().disable();
    }
	
	@Override
	protected void configure(AuthenticationManagerBuilder auth) throws Exception{
		
		log.info("configure");
		auth.userDetailsService(customUserService()).passwordEncoder(passwordEncoder());
		//auth.inMemoryAuthentication().withUser("admin").password("{noop}admin").roles("ADMIN");
		/*
		auth
		.jdbcAuthentication().dataSource(dataSource)
		.userDetailsService(customUserService())
		.passwordEncoder(passwordEncoder());*/
	}
	
	
	
}