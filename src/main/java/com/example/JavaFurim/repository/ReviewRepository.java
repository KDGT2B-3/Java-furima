package com.example.JavaFurim.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.JavaFurim.entity.Review;
import com.example.JavaFurim.entity.User;

@Repository
public interface ReviewRepository extends JpaRepository<Review, Long> {
	List<Review> findBySeller(User seller);

	Optional<Review> findByOrderId(Long orderId);

	List<Review> findByReviewer(User reviewer);
}