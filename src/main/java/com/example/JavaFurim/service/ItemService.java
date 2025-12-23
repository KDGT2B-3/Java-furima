package com.example.JavaFurim.service;

import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.example.JavaFurim.entity.Item;
import com.example.JavaFurim.entity.User;
import com.example.JavaFurim.entity.UserComplaint;
import com.example.JavaFurim.repository.ItemRepository;
import com.example.JavaFurim.repository.UserComplaintRepository;

@Service
public class ItemService {

	private final ItemRepository itemRepository;
	private final CategoryService categoryService;
	private final CloudinaryService cloudinaryService;
	private final UserComplaintRepository userComplaintRepository;

	// ===== 転売・不正対策用定数 =====
	private static final BigDecimal MAX_RATE = new BigDecimal("1.8");
	private static final int MAX_ITEMS_PER_7_DAYS = 5;
	private static final Long SYSTEM_REPORTER_ID = 0L; // 0 = システム自動通報
	// ==============================

	public ItemService(
			ItemRepository itemRepository,
			CategoryService categoryService,
			CloudinaryService cloudinaryService,
			UserComplaintRepository userComplaintRepository) {

		this.itemRepository = itemRepository;
		this.categoryService = categoryService;
		this.cloudinaryService = cloudinaryService;
		this.userComplaintRepository = userComplaintRepository;
	}

	// ===== 検索系 =====

	public Page<Item> searchItems(String keyword, Long categoryId, int page, int size) {

		Pageable pageable = PageRequest.of(page, size);

		if (keyword != null && !keyword.isEmpty() && categoryId != null) {
			return itemRepository.findByNameContainingIgnoreCaseAndCategoryIdAndStatus(
					keyword, categoryId, "出品中", pageable);
		} else if (keyword != null && !keyword.isEmpty()) {
			return itemRepository.findByNameContainingIgnoreCaseAndStatus(
					keyword, "出品中", pageable);
		} else if (categoryId != null) {
			return itemRepository.findByCategoryIdAndStatus(
					categoryId, "出品中", pageable);
		} else {
			return itemRepository.findByStatus("出品中", pageable);
		}
	}

	// ===== 取得系 =====

	public List<Item> getAllItems() {
		return itemRepository.findAll();
	}

	public Optional<Item> getItemById(Long id) {
		return itemRepository.findById(id);
	}

	public List<Item> getItemsBySeller(User seller) {
		return itemRepository.findBySeller(seller);
	}

	// ===== 出品処理（転売対策の中核） =====

	public Item saveItem(Item item, MultipartFile imageFile) throws IOException {

		// createdAt を明示的にセット（短期間判定の信頼性確保）
		item.setCreatedAt(LocalDateTime.now());

		// --- 転売対策①：価格の上限（1.8倍） ---
		BigDecimal limit = item.getOriginalPrice().multiply(MAX_RATE);
		if (item.getPrice().compareTo(limit) > 0) {
			throw new IllegalArgumentException(
					"出品価格は元値の1.8倍までにしてください");
		}

		// --- 転売対策②：短期間・大量出品（新規出品のみ） ---
		if (item.getId() == null) {

			LocalDateTime sevenDaysAgo = LocalDateTime.now().minusDays(7);

			long count = itemRepository.countRecentItemsBySellerAndCategory(
					item.getSeller(),
					item.getCategory(),
					sevenDaysAgo);

			if (count >= MAX_ITEMS_PER_7_DAYS) {

				// システムによる自動通報
				UserComplaint complaint = new UserComplaint();
				complaint.setReportedUserId(item.getSeller().getId());
				complaint.setReporterUserId(SYSTEM_REPORTER_ID);
				complaint.setReason(
						"短期間・大量出品の疑い（7日以内に同一カテゴリで6件目の出品を試行）");

				userComplaintRepository.save(complaint);

				throw new IllegalArgumentException(
						"同一カテゴリの商品は7日以内に5件までしか出品できません。");
			}
		}

		// --- 画像アップロード ---
		if (imageFile != null && !imageFile.isEmpty()) {
			String imageUrl = cloudinaryService.uploadFile(imageFile);
			item.setImageUrl(imageUrl);
		}

		return itemRepository.save(item);
	}

	// ===== 更新・削除系 =====

	public void deleteItem(Long id) {
		itemRepository.findById(id).ifPresent(item -> {
			if (item.getImageUrl() != null) {
				try {
					cloudinaryService.deleteFile(item.getImageUrl());
				} catch (IOException e) {
					System.err.println(
							"Failed to delete image from Cloudinary: " + e.getMessage());
				}
			}
			itemRepository.deleteById(id);
		});
	}

	public void markItemAsSold(Long itemId) {
		itemRepository.findById(itemId).ifPresent(item -> {
			item.setStatus("売却済");
			itemRepository.save(item);
		});
	}
}
