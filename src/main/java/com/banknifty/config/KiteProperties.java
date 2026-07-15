package com.banknifty.config;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@Getter
@Setter
@Validated
@ConfigurationProperties(prefix = "kite")
public class KiteProperties {

    /**
     * Zerodha API Key
     */
    @NotBlank
    private String apiKey;

    /**
     * Zerodha API Secret
     */
    @NotBlank
    private String apiSecret;

    /**
     * Daily generated Access Token
     */
    @NotBlank
    private String accessToken;

    /**
     * Request timeout (seconds)
     */
    private int timeout = 30;

    /**
     * Maximum retry attempts
     */
    private int retryCount = 3;

}