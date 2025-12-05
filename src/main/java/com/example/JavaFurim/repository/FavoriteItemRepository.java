package com.example.JavaFurim.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.JavaFurim.entity.FavoriteItem;
import com.example.JavaFurim.entity.Item;
import com.example.JavaFurim.entity.User;

@Repository
public interface FavoriteItemRepository extends JpaRepository<FavoriteItem, Long> {
	Optional<FavoriteItem> findByUserAndItem(User user, Item item);

	List<FavoriteItem> findByUser(User user);

	boolean existsByUserAndItem(User user, Item item);
}