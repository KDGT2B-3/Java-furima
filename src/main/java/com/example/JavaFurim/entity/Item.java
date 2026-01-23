package com.example.JavaFurim.entity;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 商品エンティティ
 * JPA エンティティとして DB テーブル "item" と対応
 */
@Entity
@Table(name = "item")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Item {

	// 主キー：DB 側で自動採番 (SERIAL/IDENTITY)
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	// 出品者：User との多対一関係 (user_id 列で紐付け)
	@ManyToOne
	@JoinColumn(name = "user_id", nullable = false)
	private User seller;

	// 商品名
	@Column(nullable = false)
	private String name;

	// 商品説明 (TEXT 型)
	@Column(columnDefinition = "TEXT")
	private String description;

	// 販売価格
	@Column(nullable = false)
	private BigDecimal price;

	@Column(nullable = false)
	private BigDecimal originalPrice;

	// カテゴリ：Category との多対一関係 (category_id 列)
	@ManyToOne
	@JoinColumn(name = "category_id")
	private Category category;

	// 商品ステータス（初期値: 出品中）
	@Column(nullable = false)
	private String status = "出品中";

	// 商品画像の URL
	private String imageUrl;

	// 登録日時
	@Column(name = "created_at", nullable = false)
	private LocalDateTime createdAt = LocalDateTime.now();
}