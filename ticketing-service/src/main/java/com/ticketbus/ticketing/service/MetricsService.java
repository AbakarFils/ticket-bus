package com.ticketbus.ticketing.service;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Tags;
import io.micrometer.core.instrument.Timer;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Service
@RequiredArgsConstructor
public class MetricsService {

    private final MeterRegistry meterRegistry;

    // Counters for business metrics
    private static final String TICKETS_PURCHASED_COUNTER = "tickets.purchased.total";
    private static final String TICKETS_FAILED_COUNTER = "tickets.purchase.failed.total";
    private static final String REVENUE_COUNTER = "tickets.revenue.total";
    private static final String QR_GENERATED_COUNTER = "qr.generated.total";
    private static final String WALLET_INSUFFICIENT_COUNTER = "wallet.insufficient.total";

    // Timers for performance metrics
    private static final String TICKET_PURCHASE_TIMER = "tickets.purchase.duration";
    private static final String QR_GENERATION_TIMER = "qr.generation.duration";

    /**
     * Record successful ticket purchase
     */
    public void recordTicketPurchased(String productType, String paymentMethod, BigDecimal price) {
        Counter.builder(TICKETS_PURCHASED_COUNTER)
                .description("Total number of tickets purchased")
                .tag("product_type", productType)
                .tag("payment_method", paymentMethod)
                .register(meterRegistry)
                .increment();

        // Track revenue
        meterRegistry.counter(REVENUE_COUNTER,
                "product_type", productType,
                "currency", "EUR")
                .increment(price.doubleValue());
    }

    /**
     * Record failed ticket purchase
     */
    public void recordTicketPurchaseFailed(String productType, String paymentMethod, String failureReason) {
        Counter.builder(TICKETS_FAILED_COUNTER)
                .description("Total number of failed ticket purchases")
                .tag("product_type", productType)
                .tag("payment_method", paymentMethod)
                .tag("reason", failureReason)
                .register(meterRegistry)
                .increment();
    }

    /**
     * Record insufficient wallet balance
     */
    public void recordInsufficientWalletBalance(Long userId) {
        Counter.builder(WALLET_INSUFFICIENT_COUNTER)
                .description("Number of purchases failed due to insufficient wallet balance")
                .tag("user_id", userId.toString())
                .register(meterRegistry)
                .increment();
    }

    /**
     * Record QR code generation
     */
    public void recordQrGenerated(String qrType) {
        Counter.builder(QR_GENERATED_COUNTER)
                .description("Total number of QR codes generated")
                .tag("qr_type", qrType)
                .register(meterRegistry)
                .increment();
    }

    /**
     * Time ticket purchase operation
     */
    public Timer.Sample startTicketPurchaseTimer() {
        return Timer.start(meterRegistry);
    }

    /**
     * Record ticket purchase duration
     */
    public void recordTicketPurchaseDuration(Timer.Sample sample, String productType, String paymentMethod) {
        sample.stop(Timer.builder(TICKET_PURCHASE_TIMER)
                .description("Time taken to purchase a ticket")
                .tag("product_type", productType)
                .tag("payment_method", paymentMethod)
                .register(meterRegistry));
    }

    /**
     * Time QR generation operation
     */
    public Timer.Sample startQrGenerationTimer() {
        return Timer.start(meterRegistry);
    }

    /**
     * Record QR generation duration
     */
    public void recordQrGenerationDuration(Timer.Sample sample, String qrType) {
        sample.stop(Timer.builder(QR_GENERATION_TIMER)
                .description("Time taken to generate QR code")
                .tag("qr_type", qrType)
                .register(meterRegistry));
    }

    /**
     * Record custom gauge metric
     */
    public void recordActiveTicketsGauge(String productType, int count) {
        meterRegistry.gauge("tickets.active.count",
                Tags.of("product_type", productType),
                count);
    }
}
