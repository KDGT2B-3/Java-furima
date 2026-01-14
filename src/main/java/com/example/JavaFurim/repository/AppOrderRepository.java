package com.example.JavaFurim.repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.example.JavaFurim.entity.AppOrder;
import com.example.JavaFurim.entity.Category;
import com.example.JavaFurim.entity.User;

@Repository
public interface AppOrderRepository extends JpaRepository<AppOrder, Long> {
	List<AppOrder> findByBuyer(User buyer);

	List<AppOrder> findByItem_Seller(User seller);

	Optional<AppOrder> findByPaymentIntentId(String paymentIntentId);

	@Query("""
				SELECT COUNT(o)
				FROM AppOrder o
				WHERE o.buyer = :buyer
				  AND o.item.category = :category
				  AND o.createdAt >= :fromTime
			""")
	long countRecentPurchasesByBuyerAndCategory(
			@Param("buyer") User buyer,
			@Param("category") Category category,
			@Param("fromTime") LocalDateTime fromTime);
}
