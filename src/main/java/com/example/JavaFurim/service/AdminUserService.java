package com.example.JavaFurim.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.NoSuchElementException;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.JavaFurim.entity.User;
import com.example.JavaFurim.entity.UserComplaint;
import com.example.JavaFurim.repository.UserComplaintRepository;
import com.example.JavaFurim.repository.UserRepository;

@Service
public class AdminUserService {
	private final UserRepository userRepository;
	private final UserComplaintRepository userComplaintRepository;

	public AdminUserService(UserRepository userRepository, UserComplaintRepository userComplaintRepository) {
		this.userRepository = userRepository;
		this.userComplaintRepository = userComplaintRepository;
	}

	public List<User> listAllUsers() {
		return userRepository.findAll();
	}

	public User findUser(Long id) {
		return userRepository.findById(id).orElseThrow(() -> new NoSuchElementException("User not found: " + id));
	}

	public Double averageRating(Long userId) {
		Double avg = userRepository.averageRatingForUser(userId);
		return (avg == null) ? 0.0 : avg;
	}

	public long complaintCount(Long userId) {

		return userComplaintRepository.countByReportedUserId(userId);
	}

	public List<UserComplaint> complaints(Long userId) {

	public List<UserComplaint> complaints(Long userId) {
		return userComplaintRepository.findByReportedUserIdOrderByCreatedAtDesc(userId);
	}

	@Transactional
	public void banUser(Long targetUserId, Long adminUserId, String reason, boolean alsoDisableLogin) {
		User u = findUser(targetUserId);
		u.setBan(true);
		u.setBanReason(reason);
		u.setBannedAt(LocalDateTime.now());
		u.setBannedByAdminId(adminUserId == null ? null : adminUserId.intValue());
		if (alsoDisableLogin)
			u.setEnabled(false);
		userRepository.save(u);
	}

	@Transactional
	public void unbanUser(Long targetUserId) {
		User u = findUser(targetUserId);
		u.setBanned(false);
		u.setBanReason(null);
		u.setBannedAt(null);
		u.setBannedByAdminId(null);
		u.setEnabled(true); // BAN 解除後ログイン有効化
		userRepository.save(u);
	}
}
