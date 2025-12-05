package com.example.JavaFurim.repository;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.JavaFurim.entity.Item;
import com.example.JavaFurim.entity.User;

@Repository
public interface ItemRepository extends JpaRepository<Item, Long> {
	Page<Item> findByNameContainingIgnoreCaseAndStatus(String name, String status, Pageable pageable);

	Page<Item> findByCategoryIdAndStatus(Long categoryId, String status, Pageable pageable);

	// 名前の部分一致 + カテゴリ ID + ステータスでページング検索
	Page<Item> findByNameContainingIgnoreCaseAndCategoryIdAndStatus(String name, Long categoryId, String status,
			Pageable pageable);

	// ステータスのみでページング取得（公開中一覧など）
	Page<Item> findByStatus(String status, Pageable pageable);

	// 出品者ごとの商品一覧
	List<Item> findBySeller(User seller);
}