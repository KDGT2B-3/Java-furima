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

	// パスワードエンコーダーを定義（これがないと認証が動きません）
	@Bean
	public PasswordEncoder passwordEncoder() {
		return PasswordEncoderFactories.createDelegatingPasswordEncoder();
	}

	@Bean
	public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
		http
				.authorizeHttpRequests(auth -> auth
						// 静的リソースとログイン画面は全員許可
						.requestMatchers("/login", "/css/**", "/js/**", "/images/**", "/webjars/**").permitAll()
						// 管理者専用パス
						.requestMatchers("/admin/**").hasRole("ADMIN")
						// それ以外はすべて認証（ログイン）が必要
						.anyRequest().authenticated())
				.formLogin(form -> form
						.loginPage("/login")
						// ログイン成功時にカスタムハンドラー（下のBean）を呼び出す
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