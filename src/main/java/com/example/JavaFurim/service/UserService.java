package com.example.JavaFurim.service;

import java.util.List;
import java.util.Optional;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.JavaFurim.entity.User;
import com.example.JavaFurim.repository.UserRepository;

@Service
public class UserService {

	private final UserRepository userRepository;
	private final PasswordEncoder passwordEncoder;

	// コンストラクタ注入
	public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
		this.userRepository = userRepository;
		this.passwordEncoder = passwordEncoder;
	}

	public List<User> getAllUsers() {
		return userRepository.findAll();
	}

	public Optional<User> getUserById(Long id) {
		return userRepository.findById(id);
	}

	public Optional<User> getUserByEmail(String email) {
		return userRepository.findByEmail(email);
	}

	@Transactional
	public User saveUser(User user) {
		return userRepository.save(user);
	}

	@Transactional
	public void deleteUser(Long id) {
		userRepository.deleteById(id);
	}

	@Transactional
	public void toggleUserEnabled(Long userId) {
		User user = userRepository.findById(userId)
				.orElseThrow(() -> new IllegalArgumentException("User not found"));
		user.setEnabled(!user.isEnabled());
		userRepository.save(user);
	}

	@Transactional
	public User registerNewUser(String name, String email, String password, String confirmPassword) {
		// パスワード一致チェック
		if (!password.equals(confirmPassword)) {
			throw new IllegalArgumentException("パスワードが一致しません");
		}

		// メール重複チェック
		if (userRepository.existsByEmailIgnoreCase(email)) {
			throw new IllegalArgumentException("このメールアドレスは既に登録されています");
		}

		// 入力チェック
		if (name == null || name.trim().isEmpty()) {
			throw new IllegalArgumentException("名前を入力してください");
		}
		if (email == null || email.trim().isEmpty()) {
			throw new IllegalArgumentException("メールアドレスを入力してください");
		}
		if (password == null || password.length() < 6) {
			throw new IllegalArgumentException("パスワードは6文字以上で入力してください");
		}

		// User エンティティを作成
		User user = new User();
		user.setName(name.trim());
		user.setEmail(email.trim().toLowerCase());
		// パスワードをハッシュ化して保存
		user.setPassword(passwordEncoder.encode(password));
		user.setRole("USER");
		user.setEnabled(true);
		user.setBanned(false);

		return userRepository.save(user);
	}
}