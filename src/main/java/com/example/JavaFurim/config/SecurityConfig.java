package com.example.JavaFurim.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

// このクラスが Spring の設定クラスであることを示すアノテーション
@Configuration
// メソッド単位のセキュリティ（@PreAuthorize など）を有効化するアノテーション
@EnableMethodSecurity(prePostEnabled = true)
public class SecurityConfig {

	// PasswordEncoder を DI コンテナに登録する Bean 定義
	@Bean
	public PasswordEncoder passwordEncoder() {
		// 委譲型の PasswordEncoder を生成（{bcrypt} や {noop} などの ID 付きエンコードに対応）
		return PasswordEncoderFactories.createDelegatingPasswordEncoder();
	}

	// Spring Security のフィルタチェーンを定義する Bean
	@Bean
	public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
		// HttpSecurity に対して認可・ログイン・ログアウト・CSRF 設定を行う
		http
				// 認可（アクセス制御）に関する設定を行う
				.authorizeHttpRequests(auth -> auth
						// ログイン画面および静的リソースへのアクセスパスを指定
						.requestMatchers("/login", "/css/**", "/js/**", "/images/**", "/webjars/**")
						// 上記パスは未ログインでもアクセス許可
						.permitAll()
						// /admin 配下は ADMIN ロールのみアクセス許可
						.requestMatchers("/admin/**").hasRole("ADMIN")
						// それ以外の全てのリクエストは認証済みユーザーのみアクセス許可
						.anyRequest().authenticated())
				// フォームログインの設定を行う
				.formLogin(form -> form
						// カスタムログインページの URL を指定
						.loginPage("/login")
						// ログイン成功後の遷移先 URL（/items）を強制的に指定
						.defaultSuccessUrl("/items", true)
						// ログインページへのアクセスを未ログインでも許可
						.permitAll())
				// ログアウト処理の設定を行う
				.logout(logout -> logout
						// ログアウト処理を実行する URL（通常は POST /logout）
						.logoutUrl("/logout")
						// ログアウト成功後にリダイレクトする URL（クエリパラメータで状態を付与）
						.logoutSuccessUrl("/login?logout")
						// ログアウト URL へのアクセスを許可
						.permitAll())
				// CSRF 対策をデフォルト設定で有効化
				.csrf(Customizer.withDefaults());

		// 設定を反映した SecurityFilterChain インスタンスを生成して返却
		return http.build();
	}
}