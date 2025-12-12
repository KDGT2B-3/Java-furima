package com.example.JavaFurim.controller;

import java.util.Comparator;
import java.util.List;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.example.JavaFurim.entity.User;
import com.example.JavaFurim.repository.UserRepository;
import com.example.JavaFurim.service.AdminUserService;

@Controller
@RequestMapping("/admin/users")
@PreAuthorize("hasRole('ADMIN')")
public class AminUserController {
	private final AdminUserService service;
	private final UserRepository users;

	public AdminUserController(AdminUserService service, UserRepository users) {
		this.service = service;
		this.users = users;
	}

	@GetMapping
	public String list(@RequestParam(value = "q", required = false) String q,
			@RequestParam(value = "sort", required = false, defaultValue = "id") String sort, Model model) {
		List<User> list = service.listAllUsers();
		if (StringUtils.hasText(q)) {
			String qq = q.toLowerCase();
			list = list.stream().filter(u -> (u.getName() != null &&
					u.getName().toLowerCase().contains(qq)) ||
					(u.getEmail() != null &&
							u.getEmail().toLowerCase().contains(qq)))
					.toList();
		}
		list = switch (sort) {
		case "name" -> list.stream().sorted(Comparator.comparing(User::getName,
				Comparator.nullsLast(String::compareToIgnoreCase))).toList();
		case "email" -> list.stream().sorted(Comparator.comparing(User::getEmail,
				Comparator.nullsLast(String::compareToIgnoreCase))).toList();
		case "banned" -> list.stream()
				.sorted(Comparator.comparing(User::isBanned).reversed()).toList();
		default -> list;
		};
		model.addAttribute("users", list);
		model.addAttribute("q", q);
		model.addAttribute("sort", sort);
		return "admin/users/list";
	}

}
