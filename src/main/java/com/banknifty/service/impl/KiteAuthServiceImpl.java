package com.banknifty.service.impl;

import com.banknifty.entity.KiteSessionEntity;
import com.banknifty.gateway.KiteAuthenticationGateway;
import com.banknifty.model.KiteLoginResponse;
import com.banknifty.model.KiteSessionResponse;
import com.banknifty.repository.KiteSessionRepository;
import com.banknifty.service.KiteAuthService;
import com.zerodhatech.models.User;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Slf4j
@Service
@RequiredArgsConstructor
public class KiteAuthServiceImpl implements KiteAuthService {

	@Value("${kite.api-key}")
	private String apiKey;

	@Value("${kite.redirect-url}")
	private String redirectUrl;

	private final KiteAuthenticationGateway authenticationGateway;
	private final KiteSessionRepository sessionRepository;

	@PostConstruct
	void restoreActiveSession() {
		sessionRepository.findByActiveTrue().ifPresent(session -> {
			authenticationGateway.restoreSession(session.getAccessToken(), session.getUserId());
			log.info("Restored saved Kite session for {}", session.getUserId());
		});
	}

	@Override
	public KiteLoginResponse login() {
		String loginUrl = "https://kite.zerodha.com/connect/login?v=3&api_key=" + apiKey;
		return KiteLoginResponse.builder().apiKey(apiKey).loginUrl(loginUrl).redirectUrl(redirectUrl).build();
	}

	@Override
	public KiteSessionResponse callback(String requestToken) {
		User user = authenticationGateway.generateSession(requestToken);
		deactivateOldSessions();

		KiteSessionEntity entity = KiteSessionEntity.builder().userId(user.userId).userName(user.userName)
				.accessToken(user.accessToken).publicToken(user.publicToken).loginTime(LocalDateTime.now()).active(true)
				.build();

		sessionRepository.save(entity);
		log.info("Kite session saved successfully for {}", entity.getUserId());

		return KiteSessionResponse.builder().success(true).userId(entity.getUserId()).userName(entity.getUserName())
				.loginTime(entity.getLoginTime()).active(true).message("Login Successful").build();
	}

	@Override
	public KiteSessionResponse currentSession() {
		return sessionRepository.findByActiveTrue()
				.map(session -> KiteSessionResponse.builder().success(true).userId(session.getUserId())
						.userName(session.getUserName()).loginTime(session.getLoginTime()).active(true)
						.message("Active Session").build())
				.orElse(KiteSessionResponse.builder().success(false).active(false).message("No Active Session")
						.build());
	}

	@Override
	public void logout() {
		sessionRepository.findByActiveTrue().ifPresent(session -> {
			session.setActive(false);
			sessionRepository.save(session);
			log.info("Kite session deactivated for {}", session.getUserId());
		});
	}

	private void deactivateOldSessions() {
		sessionRepository.findAll().forEach(session -> {
			if (session.isActive()) {
				session.setActive(false);
				sessionRepository.save(session);
			}
		});
	}
}
