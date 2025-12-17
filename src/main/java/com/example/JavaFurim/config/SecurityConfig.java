package com.example.JavaFurim.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true)
public class SecurityConfig {

	@Bean
	public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
		http
				.authorizeHttpRequests(authorize -> authorize
						// 誰でもアクセスできるページ（ログイン、静的ファイル、商品一覧）
						.requestMatchers("/login", "/css/**", "/js/**", "/images/**", "/items/**").permitAll()
						.requestMatchers("/orders/stripe-webhook").permitAll()
						// 管理者のみ
						.requestMatchers("/admin/**").hasRole("ADMIN")
						// それ以外はログインが必要
						.anyRequest().authenticated())
				.formLogin(form -> form
						.loginPage("/login")
						// HTMLの <input name="email"> と一致させるための設定
						.usernameParameter("email")
						.defaultSuccessUrl("/items", true)
						.permitAll())
				.logout(logout -> logout
						.logoutUrl("/logout")
						.logoutSuccessUrl("/login?logout")
						.permitAll())
				.csrf(csrf -> csrf
						.ignoringRequestMatchers("/orders/stripe-webhook"));

		return http.build();
	}

	// パスワードを BCrypt 形式で照合する（DBの $2a$10$... と一致させる）
	@Bean
	public PasswordEncoder passwordEncoder() {
		return new BCryptPasswordEncoder();
	}
}