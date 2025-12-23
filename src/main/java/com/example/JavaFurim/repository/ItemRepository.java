package com.example.JavaFurim.repository;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.example.JavaFurim.entity.Category;
import com.example.JavaFurim.entity.Item;
import com.example.JavaFurim.entity.User;

@Repository
public interface ItemRepository extends JpaRepository<Item, Long> {

	// ===== 既存機能 =====

	Page<Item> findByNameContainingIgnoreCaseAndStatus(
			String name, String status, Pageable pageable);

	Page<Item> findByCategoryIdAndStatus(
			Long categoryId, String status, Pageable pageable);

	Page<Item> findByNameContainingIgnoreCaseAndCategoryIdAndStatus(
			String name, Long categoryId, String status, Pageable pageable);

	Page<Item> findByStatus(String status, Pageable pageable);

	List<Item> findBySeller(User seller);

	// ===== 追加：短期間・大量出品チェック用 =====

	@Query("""
			    SELECT COUNT(i)
			    FROM Item i
			    WHERE i.seller = :seller
			      AND i.category = :category
			      AND i.createdAt >= :fromDate
			""")
	long countRecentItemsBySellerAndCategory(
			@Param("seller") User seller,
			@Param("category") Category category,
			@Param("fromDate") LocalDateTime fromDate);
}
