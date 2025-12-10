package com.example.JavaFurim.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.JavaFurim.entity.UserComplaint;

public interface UserComplaintRepository extends JpaRepository<UserComplaint, Long> {

	long countByReportedUserId(Long reportedUserId);

	List<UserComplaint> findByReportedUserIdOrderByCreatedAtDesc(Long reportedUserId);
}
