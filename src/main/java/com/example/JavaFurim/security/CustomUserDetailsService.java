package com.example.JavaFurim.security;

import java.util.List;

import org.springframework.security.authentication.DisabledException;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import com.example.JavaFurim.entity.User;
import com.example.JavaFurim.repository.UserRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

	private final UserRepository users;

	@Override
	public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
		// findByEmailIgnoreCase で DB からユーザーを探す
		User u = users.findByEmailIgnoreCase(username)
				.orElseThrow(() -> new UsernameNotFoundException("User not found: " + username));

		// アカウントが有効かチェック
		if (!u.isEnabled()) {
			throw new DisabledException("Account disabled");
		}

		// BAN されているかチェック
		if (u.isBanned()) {
			throw new DisabledException("Account banned");
		}

		// Spring Security 用のユーザーオブジェクトを返す
		return new org.springframework.security.core.userdetails.User(
				u.getEmail(),
				u.getPassword(),
				List.of(new SimpleGrantedAuthority("ROLE_" + u.getRole())));
	}
}