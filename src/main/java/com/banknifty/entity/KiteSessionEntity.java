package com.banknifty.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "kite_session")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class KiteSessionEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(nullable = false)
	private String userId;

	@Column(nullable = false)
	private String userName;

	@Column(nullable = false, length = 2048)
	private String accessToken;

	@Column(length = 2048)
	private String publicToken;

	@Column(nullable = false)
	private LocalDateTime loginTime;

	@Column(nullable = false)
	private boolean active;

}