package com.banknifty.controller;

import java.net.URI;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.banknifty.model.KiteLoginResponse;
import com.banknifty.model.KiteSessionResponse;
import com.banknifty.service.KiteAuthService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/kite")
@RequiredArgsConstructor
public class KiteAuthController {

	private static final String FRONTEND_URL = "http://localhost:5173/";

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
	 *
	 * Zerodha redirects the browser here with request_token. After creating the
	 * Kite session successfully, redirect the browser back to the React
	 * application.
	 */
	@GetMapping("/callback")
	public ResponseEntity<Void> callback(@RequestParam("request_token") String requestToken) {

		kiteAuthService.callback(requestToken);

		return ResponseEntity.status(HttpStatus.FOUND).location(URI.create(FRONTEND_URL)).build();
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