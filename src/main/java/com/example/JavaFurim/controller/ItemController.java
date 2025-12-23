package com.example.JavaFurim.controller;

import java.io.IOException;
import java.math.BigDecimal;

import org.springframework.data.domain.Page;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import com.example.JavaFurim.entity.Category;
import com.example.JavaFurim.entity.Item;
import com.example.JavaFurim.entity.User;
import com.example.JavaFurim.service.CategoryService;
import com.example.JavaFurim.service.ChatService;
import com.example.JavaFurim.service.FavoriteService;
import com.example.JavaFurim.service.ItemService;
import com.example.JavaFurim.service.ReviewService;
import com.example.JavaFurim.service.UserService;

@Controller
@RequestMapping("/items")
public class ItemController {

	private final ItemService itemService;
	private final CategoryService categoryService;
	private final UserService userService;
	private final ChatService chatService;
	private final FavoriteService favoriteService;
	private final ReviewService reviewService;

	public ItemController(
			ItemService itemService,
			CategoryService categoryService,
			UserService userService,
			ChatService chatService,
			FavoriteService favoriteService,
			ReviewService reviewService) {
		this.itemService = itemService;
		this.categoryService = categoryService;
		this.userService = userService;
		this.chatService = chatService;
		this.favoriteService = favoriteService;
		this.reviewService = reviewService;
	}

	/* ===================== 商品一覧 ===================== */

	@GetMapping
	public String listItems(
			@RequestParam(value = "keyword", required = false) String keyword,
			@RequestParam(value = "categoryId", required = false) Long categoryId,
			@RequestParam(value = "page", defaultValue = "0") int page,
			@RequestParam(value = "size", defaultValue = "10") int size,
			Model model) {

		Page<Item> items = itemService.searchItems(keyword, categoryId, page, size);
		model.addAttribute("items", items);
		model.addAttribute("categories", categoryService.getAllCategories());
		return "item_list";
	}

	/* ===================== 商品詳細 ===================== */

	@GetMapping("/{id}")
	public String showItemDetail(
			@PathVariable Long id,
			@AuthenticationPrincipal UserDetails userDetails,
			Model model) {

		Item item = itemService.getItemById(id)
				.orElseThrow(() -> new RuntimeException("Item not found"));

		model.addAttribute("item", item);
		model.addAttribute("chats", chatService.getChatMessagesByItem(id));

		reviewService.getAverageRatingForSeller(item.getSeller())
				.ifPresent(avg -> model.addAttribute("sellerAverageRating",
						String.format("%.1f", avg)));

		if (userDetails != null) {
			User currentUser = userService.getUserByEmail(userDetails.getUsername())
					.orElseThrow();
			model.addAttribute("isFavorited",
					favoriteService.isFavorited(currentUser, id));
		}

		return "item_detail";
	}

	/* ===================== 出品フォーム ===================== */

	@GetMapping("/new")
	public String showAddItemForm(Model model) {
		model.addAttribute("item", new Item());
		model.addAttribute("categories", categoryService.getAllCategories());
		return "item_form";
	}

	/* ===================== 出品処理 ===================== */

	@PostMapping
	public String addItem(
			@AuthenticationPrincipal UserDetails userDetails,
			@RequestParam String name,
			@RequestParam String description,
			@RequestParam BigDecimal price,
			@RequestParam BigDecimal originalPrice,
			@RequestParam Long categoryId,
			@RequestParam(value = "image", required = false) MultipartFile imageFile,
			Model model) {

		User seller = userService.getUserByEmail(userDetails.getUsername())
				.orElseThrow();

		Category category = categoryService.getCategoryById(categoryId)
				.orElseThrow();

		Item item = new Item();
		item.setSeller(seller);
		item.setName(name);
		item.setDescription(description);
		item.setPrice(price);
		item.setOriginalPrice(originalPrice);
		item.setCategory(category);

		try {
			itemService.saveItem(item, imageFile);
			return "redirect:/items";

		} catch (IllegalArgumentException e) {
			// 価格1.8倍超過など
			model.addAttribute("errorMessage", e.getMessage());
			model.addAttribute("item", item);
			model.addAttribute("categories", categoryService.getAllCategories());
			return "item_form";

		} catch (IOException e) {
			model.addAttribute("errorMessage", "画像のアップロードに失敗しました");
			model.addAttribute("item", item);
			model.addAttribute("categories", categoryService.getAllCategories());
			return "item_form";
		}
	}

	/* ===================== 編集フォーム ===================== */

	@GetMapping("/{id}/edit")
	public String showEditItemForm(@PathVariable Long id, Model model) {
		Item item = itemService.getItemById(id)
				.orElseThrow();
		model.addAttribute("item", item);
		model.addAttribute("categories", categoryService.getAllCategories());
		return "item_form";
	}

	/* ===================== 更新処理 ===================== */

	@PostMapping("/{id}")
	public String updateItem(
			@PathVariable Long id,
			@AuthenticationPrincipal UserDetails userDetails,
			@RequestParam String name,
			@RequestParam String description,
			@RequestParam BigDecimal price,
			@RequestParam Long categoryId,
			@RequestParam(value = "image", required = false) MultipartFile imageFile,
			Model model) {

		Item item = itemService.getItemById(id)
				.orElseThrow();

		User currentUser = userService.getUserByEmail(userDetails.getUsername())
				.orElseThrow();

		if (!item.getSeller().getId().equals(currentUser.getId())) {
			return "redirect:/items";
		}

		item.setName(name);
		item.setDescription(description);
		item.setPrice(price);
		item.setCategory(
				categoryService.getCategoryById(categoryId).orElseThrow());

		try {
			itemService.saveItem(item, imageFile);
			return "redirect:/items/" + id;

		} catch (IllegalArgumentException e) {
			model.addAttribute("errorMessage", e.getMessage());
			model.addAttribute("item", item);
			model.addAttribute("categories", categoryService.getAllCategories());
			return "item_form";

		} catch (IOException e) {
			model.addAttribute("errorMessage", "画像のアップロードに失敗しました");
			return "item_form";
		}
	}
}
