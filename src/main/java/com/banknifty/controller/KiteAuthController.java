package com.banknifty.controller;

import com.banknifty.model.KiteLoginResponse;
import com.banknifty.model.KiteSessionResponse;
import com.banknifty.service.KiteAuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/kite")
@RequiredArgsConstructor
public class KiteAuthController {

	private final KiteAuthService kiteAuthService;

	/**
	 * Step-1 Get Zerodha Login URL.
	 */
	@GetMapping("/login")
	public KiteLoginResponse login() {

		return kiteAuthService.login();

	}

	/**
	 * Step-2 Zerodha Redirect URL.
	 */
	@GetMapping("/callback")
	public KiteSessionResponse callback(@RequestParam("request_token") String requestToken) {

		return kiteAuthService.callback(requestToken);

	}

	/**
	 * Current Session.
	 */
	@GetMapping("/session")
	public KiteSessionResponse session() {

		return kiteAuthService.currentSession();

	}

	/**
	 * Logout.
	 */
	@PostMapping("/logout")
	public void logout() {

		kiteAuthService.logout();

	}

}