package com.example.JavaFurim.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.example.JavaFurim.service.UserService;

@Controller
@RequestMapping("/register")
public class RegistrationController {

	private final UserService userService;

	public RegistrationController(UserService userService) {
		this.userService = userService;
	}

	/**
	 * 会員登録フォーム表示
	 */
	@GetMapping
	public String showRegistrationForm() {
		return "register";
	}

	/**
	 * 会員登録処理
	 */
	@PostMapping
	public String registerUser(
			@RequestParam("name") String name,
			@RequestParam("email") String email,
			@RequestParam("password") String password,
			@RequestParam("confirmPassword") String confirmPassword,
			RedirectAttributes redirectAttributes,
			Model model) {

		try {
			// ユーザー登録実行
			userService.registerNewUser(name, email, password, confirmPassword);

			// 成功メッセージを設定してログイン画面へリダイレクト
			redirectAttributes.addFlashAttribute("successMessage", "会員登録が完了しました。ログインしてください。");
			return "redirect:/login";

		} catch (IllegalArgumentException e) {
			// バリデーションエラー時はメッセージと入力値をモデルに保持してフォーム再表示
			model.addAttribute("errorMessage", e.getMessage());
			model.addAttribute("name", name);
			model.addAttribute("email", email);
			return "register";
		}
	}
}