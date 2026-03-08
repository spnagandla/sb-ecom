package com.buyology.backend.service;

import com.buyology.backend.model.Product;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class TelegramStockAlertService {

    private static final Logger log = LoggerFactory.getLogger(TelegramStockAlertService.class);

    private final WebClient webClient;

    @Value("${alerts.telegram.enabled:false}")
    private boolean telegramEnabled;

    @Value("${alerts.telegram.bot-token:}")
    private String botToken;

    @Value("${alerts.telegram.chat-id:}")
    private String chatId;

    public TelegramStockAlertService(WebClient.Builder webClientBuilder) {
        this.webClient = webClientBuilder
                .baseUrl("https://api.telegram.org")
                .build();
    }

    public void sendLowStockAlert(List<Product> lowStockProducts, int threshold) {
        if (!telegramEnabled) {
            log.info("Telegram alert is disabled. Skipping low stock notification.");
            return;
        }

        if (botToken == null || botToken.isBlank() || chatId == null || chatId.isBlank()) {
            log.warn("Telegram alert is enabled, but bot token/chat id is missing. Skipping notification.");
            return;
        }

        String alertMessage = buildAlertMessage(lowStockProducts, threshold);
        Map<String, String> payload = new HashMap<>();
        payload.put("chat_id", chatId);
        payload.put("text", alertMessage);

        String response = webClient.post()
                .uri(uriBuilder -> uriBuilder
                        .path("/bot{token}/sendMessage")
                        .build(botToken))
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(payload)
                .retrieve()
                .bodyToMono(String.class)
                .block();

        log.info("Telegram low stock alert sent successfully. Response: {}", response);
    }

    private String buildAlertMessage(List<Product> lowStockProducts, int threshold) {
        StringBuilder messageBuilder = new StringBuilder();
        messageBuilder.append("🚨 Low Stock Alert\n");
        messageBuilder.append("Products with quantity <= ").append(threshold).append(":\n\n");

        for (Product product : lowStockProducts) {
            messageBuilder.append("• ID: ").append(product.getProductId())
                    .append(" | Name: ").append(product.getProductName())
                    .append(" | Qty: ").append(product.getQuantity())
                    .append("\n");
        }

        return messageBuilder.toString();
    }
}
