package com.softwright.iam.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
public class WebSecurityConfig {

	@Bean
	public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
		
		http.cors().and().csrf().disable()
			.authorizeHttpRequests()
			.requestMatchers("/auth/*").permitAll()
			.requestMatchers("/user/*").permitAll()
			.requestMatchers("/org/*").permitAll()
			.requestMatchers("/auth/setup/callback").permitAll()
			.requestMatchers("/auth/dashboard/callback").permitAll()
			.requestMatchers("/webjars/*").permitAll();

		return http.build();
	}
	
}
