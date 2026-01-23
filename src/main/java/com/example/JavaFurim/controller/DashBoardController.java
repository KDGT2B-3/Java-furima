package com.example.JavaFurim.controller;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import com.example.JavaFurim.entity.User;
import com.example.JavaFurim.repository.UserRepository;
import com.example.JavaFurim.service.AppOrderService;
import com.example.JavaFurim.service.ItemService;

@Controller
public class DashBoardController {
	private final UserRepository userRepository;
	private final ItemService itemService;
	private final AppOrderService appOrderService;

	public DashBoardController(UserRepository userRepository, ItemService itemService,
			AppOrderService appOrderService) {
		this.userRepository = userRepository;
		this.itemService = itemService;
		this.appOrderService = appOrderService;
	}

	@GetMapping("/admin_dashboard")
	public String dashboard(
			@AuthenticationPrincipal UserDetails userDetails, Model model) {
		User currentUser = userRepository.findByEmailIgnoreCase(userDetails.getUsername())
				.orElseThrow(() -> new RuntimeException("User not found"));
		if ("ADMIN".equals(currentUser.getRole())) {
			model.addAttribute("recentItems", itemService.getAllItems());
			model.addAttribute("recentOrders", appOrderService.getAllOrders());
			return "admin/admin_dashboard";
		} else {
			return "redirect:/items";
		}
	}
}
