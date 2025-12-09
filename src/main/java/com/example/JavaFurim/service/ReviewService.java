package com.example.JavaFurim.service;

import java.util.List;
import java.util.OptionalDouble;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.JavaFurim.entity.AppOrder;
import com.example.JavaFurim.entity.Review;
import com.example.JavaFurim.entity.User;
import com.example.JavaFurim.repository.AppOrderRepository;
import com.example.JavaFurim.repository.ReviewRepository;

@Service
public class ReviewService {
	// レビューリポジトリの参照
	private final ReviewRepository reviewRepository;
	// 注文リポジトリの参照
	private final AppOrderRepository appOrderRepository;

	// 依存性をコンストラクタ注入
	public ReviewService(ReviewRepository reviewRepository, AppOrderRepository appOrderRepository) {
		this.reviewRepository = reviewRepository;

		this.appOrderRepository = appOrderRepository;
	}

	@Transactional
	public Review submitReview(Long orderId, User reviewer, int rating, String comment) {

		AppOrder order = appOrderRepository.findById(orderId)
				.orElseThrow(() -> new IllegalArgumentException("Order not found."));

		if (!order.getBuyer().getId().equals(reviewer.getId())) {
			throw new IllegalStateException("Only the buyer can review this order.");
		}
		if (reviewRepository.findByOrderId(orderId).isPresent()) {
			throw new IllegalStateException("This order has already been reviewed.");
		}
		Review review = new Review();

		review.setOrder(order);

		review.setReviewer(reviewer);

		review.setSeller(order.getItem().getSeller());

		review.setItem(order.getItem());

		review.setRating(rating);

		review.setComment(comment);

		return reviewRepository.save(review);
	}

	public List<Review> getReviewsBySeller(User seller) {
		return reviewRepository.findBySeller(seller);
	}

	public OptionalDouble getAverageRatingForSeller(User seller) {
		return reviewRepository.findBySeller(seller).stream()
				.mapToInt(Review::getRating)
				.average();
	}

	public List<Review> getReviewsByReviewer(User reviewer) {

		return reviewRepository.findByReviewer(reviewer);
	}
}