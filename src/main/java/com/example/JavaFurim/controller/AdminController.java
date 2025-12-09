package com.example.JavaFurim.controller;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

import com.example.JavaFurim.service.AppOrderService;
import com.example.JavaFurim.service.ItemService;
import com.example.JavaFurim.service.UserService;

@Controller
@RequestMapping("/admin")
@PreAuthorize("hasRole('ADMIN')")
public class AdminController {
	private final ItemService itemService;
	private final AppOrderService appOrderService;
	private final UserService userService;

}
