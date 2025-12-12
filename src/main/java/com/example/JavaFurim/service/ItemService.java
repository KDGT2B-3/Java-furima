package com.example.JavaFurim.service;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.example.JavaFurim.entity.Item;
import com.example.JavaFurim.repository.ItemRepository;

@Service
public class ItemService {

	private final ItemRepository itemRepository;

	private final CategoryService categoryService;

	private final CloudinaryService cloudinaryService;

	public ItemService(ItemRepository itemRepository, CategoryService categoryService,
			CloudinaryService cloudinaryService) {

		this.itemRepository = itemRepository;

		this.categoryService = categoryService;

		this.cloudinaryService = cloudinaryService;
	}

	public Page<Item> searchItems(String keyword, Long categoryId, int page, int size) {

		Pageable pageable = PageRequest.of(page, size);

		if (keyword != null && !keyword.isEmpty() && categoryId != null) {
			return itemRepository.findByNameContainingIgnoreCaseAndCategoryIdAndStatus(keyword, categoryId, "出品中",
					pageable);
		} else if (keyword != null && !keyword.isEmpty()) {
			return itemRepository.findByNameContainingIgnoreCaseAndStatus(keyword, "出品中", pageable);
		} else if (categoryId != null) {
			return itemRepository.findByCategoryIdAndStatus(categoryId, "出品中", pageable);
		} else {
			return itemRepository.findByStatus("出品中", pageable);
		}
	}

	// 全商品一覧を返す（管理用など）
	public List<Item> getAllItems() {
		return itemRepository.findAll();
	}

	public Optional<Item> getItemById(Long id) {
		return itemRepository.findById(id);
	}

	public Item saveItem(Item item, MultipartFile imageFile) throws IOException {
		if (imageFile != null && !imageFile.isEmpty()) {
			String imageUrl = cloudinaryService.uploadFile(imageFile);
			item.setImageUrl(imageUrl);
		}
		return itemRepository.save(item);
	}
}