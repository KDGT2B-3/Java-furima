package com.example.JavaFurim.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.JavaFurim.entity.Chat;
import com.example.JavaFurim.entity.Item;

@Repository

public interface ChatRepository extends JpaRepository<Chat, Long> {
	List<Chat> findByItemOrderByCreatedAtAsc(Item item);
}