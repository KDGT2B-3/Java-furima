package com.example.JavaFurim.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;

@Configuration
@EnableMethodSecurity(prePostEnabled = true)
public class SecurityConfig {

	@Bean
	public PasswordEncoder passwordEncoder() {
		return PasswordEncoderFactories.createDelegatingPasswordEncoder();
	}

	@Bean
	public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
		http
				.authorizeHttpRequests(auth -> auth
						// 未ログインでもアクセス可能なパス（ログイン、会員登録、静的リソース）
						.requestMatchers("/login", "/register", "/css/**", "/js/**", "/images/**", "/webjars/**")
						.permitAll()
						// 管理者専用パス
						.requestMatchers("/admin/**").hasRole("ADMIN")
						// それ以外はすべて認証（ログイン）が必要
						.anyRequest().authenticated())
				.formLogin(form -> form
						.loginPage("/login")
						// ログイン時の入力項目名を email に変更（デフォルトは username）
						.usernameParameter("email")
						// ログイン成功時の遷移先をロールごとに振り分ける
						.successHandler(customAuthenticationSuccessHandler())
						.permitAll())
				.logout(logout -> logout
						.logoutUrl("/logout")
						.logoutSuccessUrl("/login?logout")
						.permitAll())
				.csrf(Customizer.withDefaults());

		return http.build();
	}

	/**
	 * ログイン成功後の振り分けロジック
	 * 管理者は管理画面、一般ユーザーは商品一覧へリダイレクト
	 */
	@Bean
	public AuthenticationSuccessHandler customAuthenticationSuccessHandler() {
		return (request, response, authentication) -> {
			var roles = authentication.getAuthorities();

			// ROLE_ADMIN を持っている場合は管理画面へ
			if (roles.stream().anyMatch(r -> r.getAuthority().equals("ROLE_ADMIN"))) {
				response.sendRedirect("/admin/admin_dashboard");
			} else {
				// 一般ユーザーは商品一覧へ
				response.sendRedirect("/items");
			}
		};
	}
}