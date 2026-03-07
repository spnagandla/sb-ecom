package com.buyology.backend.scheduler;

import com.buyology.backend.model.Product;
import com.buyology.backend.repository.ProductRepository;
import com.buyology.backend.service.TelegramStockAlertService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class LowStockScheduler {

    private static final Logger log = LoggerFactory.getLogger(LowStockScheduler.class);

    private final ProductRepository productRepository;
    private final TelegramStockAlertService telegramStockAlertService;

    @Value("${alerts.stock.low-threshold:1}")
    private int lowStockThreshold;

    public LowStockScheduler(ProductRepository productRepository, TelegramStockAlertService telegramStockAlertService) {
        this.productRepository = productRepository;
        this.telegramStockAlertService = telegramStockAlertService;
    }

    // Runs every day at 1:50 AM by default
    @Scheduled(cron = "${alerts.stock.cron:0 15 2 * * *}")
    public void checkLowStockProducts() {
        List<Product> lowStockProducts = productRepository.findByQuantityLessThanEqual(lowStockThreshold);

        if (lowStockProducts.isEmpty()) {
            log.info("Low stock scheduler ran: no products found with quantity <= {}", lowStockThreshold);
            return;
        }

        log.warn("Low stock scheduler found {} products with quantity <= {}", lowStockProducts.size(), lowStockThreshold);
        lowStockProducts.forEach(product ->
                log.warn("Low stock alert -> productId: {}, productName: {}, quantity: {}",
                        product.getProductId(), product.getProductName(), product.getQuantity())
        );

        try {
            telegramStockAlertService.sendLowStockAlert(lowStockProducts, lowStockThreshold);
        } catch (Exception ex) {
            log.error("Failed to send Telegram low stock alert", ex);
        }
    }
}
