package com.example.JavaFurim.config;

//Bean 定義や設定用アノテーションの import
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
//メソッドレベルの認可アノテーション(@PreAuthorize 等)を有効化
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
//HTTP セキュリティを構築するための import
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
//WebSecurity を有効化するアノテーション
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
//ユーザ詳細を提供するための型
import org.springframework.security.core.userdetails.UserDetailsService;
//ユーザが見つからないときの例外
import org.springframework.security.core.userdetails.UsernameNotFoundException;
//安全なパスワードエンコーダ（BCrypt）を使うための import
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
//PasswordEncoder インターフェースの import
import org.springframework.security.crypto.password.PasswordEncoder;
//セキュリティフィルタチェーンの型
import org.springframework.security.web.SecurityFilterChain;

import com.example.JavaFurim.repository.UserRepository;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true)
public class SecurityConfig {
	// セキュリティの主要設定（エンドポイント保護 / 認証 / ログアウト / CSRF 例外）
	@Bean
	public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
		// HttpSecurity ビルダーに対して設定を積み上げる
		http
				.authorizeHttpRequests(authorize -> authorize

						.requestMatchers("/login", "/css/**", "/js/**", "/images/**", "/items/**").permitAll()
						// Stripe の Webhook は署名検証で守るため認可は permitAll（後で CSRF は除外）
						.requestMatchers("/orders/stripe-webhook").permitAll()
						// /admin 配下は ADMIN ロールのみ
						.requestMatchers("/admin/**").hasRole("ADMIN")
						// それ以外は認証必須
						.anyRequest().authenticated())
				// フォームログインの設定
				.formLogin(form -> form
						// ログインページのパスを指定
						.loginPage("/login")
						// 成功時は商品一覧へリダイレクト
						.defaultSuccessUrl("/items", true)
						// ログインページ自体は誰でも表示可能
						.permitAll())
				// ログアウト設定
				.logout(logout -> logout
						// ログアウト URL
						.logoutUrl("/logout")
						// 成功時はログイン画面へ
						.logoutSuccessUrl("/login?logout")
						// 誰でも呼べる
						.permitAll())
				// CSRF の設定（基本有効、Stripe Webhook のみ除外）
				.csrf(csrf -> csrf
						// Ant パターンで Webhook を除外
						.ignoringRequestMatchers("/orders/stripe-webhook"));
		// 構築したフィルタチェーンを返す
		return http.build();
	}

	// DB からユーザをロードして Spring Security の UserDetails に変換する
	@Bean
	public UserDetailsService userDetailsService(UserRepository userRepository) {
		return email -> userRepository.findByEmail(email)
				.map(user -> org.springframework.security.core.userdetails.User.builder()
						.username(user.getEmail())
						.password(user.getPassword())
						.roles(user.getRole())
						.disabled(!user.isEnabled())
						.build())
				.orElseThrow(() -> new UsernameNotFoundException("User not found: " + email));
	}

	// 安全なパスワードハッシュ用エンコーダ（BCrypt）を提供
	@Bean
	public PasswordEncoder passwordEncoder() {
		// 10 程度のストレングスがデフォルトで実用十分
		return new BCryptPasswordEncoder();
	}
}