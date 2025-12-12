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

@Entity
@Table(name = "app_order")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class AppOrder {
	// 主キーの定義（AUTO_INCREMENT）
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;
	// 注文が紐づく商品（必須）
	@ManyToOne
	@JoinColumn(name = "item_id", nullable = false)
	private Item item;
	// 買い手ユーザ（必須）
	@ManyToOne
	@JoinColumn(name = "buyer_id", nullable = false)
	private User buyer;
	// 決済金額のスナップショット（必須）
	@Column(nullable = false)
	private BigDecimal price;
	// 注文状態（購入済/発送済/決済待ち 等）
	@Column(nullable = false)
	private String status = "購入済";
	// Stripe の PaymentIntent ID を保持（決済と注文を 1 対 1 で特定）
	@Column(name = "payment_intent_id", unique = true)
	private String paymentIntentId;

	// 作成日時（集計用）
	@Column(name = "created_at", nullable = false)
	private LocalDateTime createdAt = LocalDateTime.now();

	public String getPaymentIntentId() {
		return paymentIntentId;
	}

	public void setPaymentIntentId(String paymentIntentId) {
		this.paymentIntentId = paymentIntentId;
	}
}
