package com.example.JavaFurim.service;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.JavaFurim.entity.FavoriteItem;
import com.example.JavaFurim.entity.Item;
import com.example.JavaFurim.entity.User;
import com.example.JavaFurim.repository.FavoriteItemRepository;
import com.example.JavaFurim.repository.ItemRepository;

@Service
public class FavoriteService {
	private final FavoriteItemRepository favoriteItemRepository;
	private final ItemRepository itemRepository;

	public FavoriteService(FavoriteItemRepository favoriteItemRepository, ItemRepository itemRepository) {
		this.favoriteItemRepository = favoriteItemRepository;
		this.itemRepository = itemRepository;
	}

	@Transactional
	public FavoriteItem addFavorite(User user, Long itemId) {

		Item item = itemRepository.findById(itemId)
				.orElseThrow(() -> new IllegalArgumentException("Item not found"));

		if (favoriteItemRepository.existsByUserAndItem(user, item)) {

			throw new IllegalStateException("Item is already favorited by this user.");
		}
		FavoriteItem favoriteItem = new FavoriteItem();

		favoriteItem.setUser(user);

		favoriteItem.setItem(item);

		return favoriteItemRepository.save(favoriteItem);
	}

	public boolean isFavorited(User user, Long itemId) {

		Item item = itemRepository.findById(itemId)
				.orElseThrow(() -> new IllegalArgumentException("Item not found"));

		return favoriteItemRepository.existsByUserAndItem(user, item);
	}

	public List<Item> getFavoriteItemsByUser(User user) {

		return favoriteItemRepository.findByUser(user).stream()
				.map(FavoriteItem::getItem)
				.collect(Collectors.toList());
	}

}
