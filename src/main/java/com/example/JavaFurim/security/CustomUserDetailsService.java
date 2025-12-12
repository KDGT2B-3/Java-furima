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

abstract

@Service
@RequiredArgsConstructor public class CustomUserDetailsService implements UserDetailsService {
	private final UserRepository users;

	@Override
	public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
		User u = users.findByEmailIgnoreCase(username)
				.orElseThrow(() -> new UsernameNotFoundException("User not found: " + username));

		if (!u.isEnabled())
			throw new DisabledException("Account disabled"); // アカウント無効化
		if (u.isBanned())
			throw new DisabledException("Account banned");

		return new org.springframework.security.core.userdetails.User(
				u.getEmail(),
				u.getPassword(),
				List.of(new SimpleGrantedAuthority("ROLE_" + u.getRole())) // 権限
		);
	}
}
