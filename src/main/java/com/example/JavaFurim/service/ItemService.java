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
import com.example.JavaFurim.repository.AppOrderRepository;
import com.example.JavaFurim.repository.ItemRepository;
import com.example.JavaFurim.repository.UserComplaintRepository;

@Service
public class ItemService {

	private final AppOrderRepository appOrderRepository;
	private final ItemRepository itemRepository;
	private final CategoryService categoryService;
	private final CloudinaryService cloudinaryService;
	private final UserComplaintRepository userComplaintRepository;

	// ===== 転売・不正対策用定数 =====
	private static final BigDecimal MAX_RATE = new BigDecimal("1.8");
	private static final int MAX_ITEMS_PER_7_DAYS = 5;
	private static final int MAX_COMPLAINTS = 5;
	private static final Long SYSTEM_REPORTER_ID = 0L;
	private static final int PURCHASE_RESTRICT_HOURS = 12;
	// ==============================

	public ItemService(
			AppOrderRepository appOrderRepository,
			ItemRepository itemRepository,
			CategoryService categoryService,
			CloudinaryService cloudinaryService,
			UserComplaintRepository userComplaintRepository) {

		this.appOrderRepository = appOrderRepository;
		this.itemRepository = itemRepository;
		this.categoryService = categoryService;
		this.cloudinaryService = cloudinaryService;
		this.userComplaintRepository = userComplaintRepository;
	}

	// ===== 商品検索 =====

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

		// ===== 新規出品時のみ createdAt 設定 =====
		if (item.getId() == null) {
			item.setCreatedAt(LocalDateTime.now());
		}

		// ===== 転売対策①：通報累積による出品停止 =====
		long complaintCount = userComplaintRepository.countByReportedUserId(
				item.getSeller().getId());

		if (complaintCount >= MAX_COMPLAINTS) {
			throw new IllegalArgumentException(
					"通報が一定数に達しているため、現在出品できません。運営にお問い合わせください。");
		}

		// ===== 転売対策②：価格つり上げ防止 =====
		BigDecimal limit = item.getOriginalPrice().multiply(MAX_RATE);
		if (item.getPrice().compareTo(limit) > 0) {
			throw new IllegalArgumentException(
					"出品価格は元値の1.8倍までにしてください。");
		}

		// ===== 転売対策③：短期間・大量出品（新規出品のみ） =====
		if (item.getId() == null) {

			LocalDateTime sevenDaysAgo = LocalDateTime.now().minusDays(7);

			long count = itemRepository.countRecentItemsBySellerAndCategory(
					item.getSeller(),
					item.getCategory(),
					sevenDaysAgo);

			// 6件目はブロック＋自動通報
			if (count >= MAX_ITEMS_PER_7_DAYS) {

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
		if (item.getId() == null) {

			LocalDateTime twelveHoursAgo = LocalDateTime.now().minusHours(PURCHASE_RESTRICT_HOURS);

			long recentPurchaseCount = appOrderRepository.countRecentPurchasesByBuyerAndCategory(
					item.getSeller(), // 出品者＝過去の購入者
					item.getCategory(),
					twelveHoursAgo);

			if (recentPurchaseCount > 0) {

				UserComplaint complaint = new UserComplaint();
				complaint.setReportedUserId(item.getSeller().getId());
				complaint.setReporterUserId(SYSTEM_REPORTER_ID);
				complaint.setReason(
						"購入後12時間以内に同一カテゴリの商品を出品しようとしたため自動制限");

				userComplaintRepository.save(complaint);

				throw new IllegalArgumentException(
						"同じカテゴリの商品を購入後12時間以内は出品できません。");
			}
		}

		// ===== 画像アップロード =====
		if (imageFile != null && !imageFile.isEmpty()) {
			String imageUrl = cloudinaryService.uploadFile(imageFile);
			item.setImageUrl(imageUrl);
		}

		return itemRepository.save(item);
	}

	// ===== 削除処理 =====

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

	// ===== 売却済み処理 =====

	public void markItemAsSold(Long itemId) {
		itemRepository.findById(itemId).ifPresent(item -> {
			item.setStatus("売却済");
			itemRepository.save(item);
		});
	}
}
