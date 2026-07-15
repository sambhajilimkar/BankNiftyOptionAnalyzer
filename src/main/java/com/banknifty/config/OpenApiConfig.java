package com.banknifty.config;

import io.swagger.v3.oas.models.ExternalDocumentation;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI bankNiftyOpenAPI() {

        Server localServer = new Server();
        localServer.setUrl("http://localhost:8080");
        localServer.setDescription("Local Development Server");

        Contact contact = new Contact();
        contact.setName("Sambhaji Limkar");
        contact.setEmail("your-email@example.com");

        License license = new License();
        license.setName("Apache 2.0");
        license.setUrl("https://www.apache.org/licenses/LICENSE-2.0");

        Info info = new Info()
                .title("BankNifty Professional Options Trading Analyzer API")
                .version("1.0.0")
                .description("""
                        Enterprise-grade Spring Boot application for analyzing
                        Bank Nifty intraday option buying opportunities using
                        multiple technical indicators and rule-based strategies.

                        Features:
                        • Zerodha Kite Connect Integration
                        • EMA / RSI / MACD / VWAP / ADX
                        • ATR
                        • SuperTrend
                        • Volume Analysis
                        • Candlestick Pattern Detection
                        • Trend Score
                        • BUY CE / BUY PE / NO TRADE Recommendation
                        """)
                .contact(contact)
                .license(license);

        return new OpenAPI()
                .info(info)
                .servers(List.of(localServer))
                .externalDocs(new ExternalDocumentation()
                        .description("Project Documentation")
                        .url("https://github.com/sambhajilimkar/BankNifty"));
    }
}