package com.example.JavaFurim.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "user")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class User {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;
	@Column(nullable = false)
	private String name;
	@Column(unique = true)
	private String email;
	@Column(nullable = false)
	private String password;
	@Column(nullable = false)
	private String role;
	@Column(name = "line_notify_token")
	private String lineNotifyToken;
	@Column(nullable = false)
	private boolean enabled = true;
}
