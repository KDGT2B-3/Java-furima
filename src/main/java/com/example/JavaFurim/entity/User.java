package com.example.JavaFurim.entity;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "users")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class User implements UserDetails { // ← UserDetailsを継承
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(nullable = false)
	private String name;

	@Column(unique = true)
	private String email;

	@Column(nullable = false)
	private String password;

	@Column(nullable = false)
	private String role; // ここに "ROLE_ADMIN" や "ROLE_USER" が入る

	@Column(name = "line_notify_token")
	private String lineNotifyToken;

	@Column(nullable = false)
	private boolean enabled = true;

	@Column(nullable = false)
	private boolean banned = false;

	@Column(name = "ban_reason")
	private String banReason;

	@Column(name = "banned_at")
	private LocalDateTime bannedAt;

	@Column(name = "banned_by_admin_id")
	private Integer bannedByAdminId;

	// --- UserDetails の実装メソッド ---

	@Override
	public Collection<? extends GrantedAuthority> getAuthorities() {
		// DBの role フィールド (ROLE_ADMIN 等) を Spring Security の権限として返す
		return List.of(new SimpleGrantedAuthority(this.role));
	}

	@Override
	public String getUsername() {
		return this.email; // ログインID（メールアドレス）を返す
	}

	@Override
	public boolean isAccountNonExpired() {
		return true;
	}

	@Override
	public boolean isAccountNonLocked() {
		return !this.banned; // BANされていたらロック扱いにする
	}

	@Override
	public boolean isCredentialsNonExpired() {
		return true;
	}

	@Override
	public boolean isEnabled() {
		return this.enabled;
	}
}