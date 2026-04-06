package com.ticketbus.validation.client;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class AuditFraudClient {

    private final RestTemplate restTemplate;

    @Value("${audit.url:http://localhost:8087}")
    private String auditUrl;

    public boolean isTicketBlacklisted(Long ticketId) {
        try {
            @SuppressWarnings("unchecked")
            Map<String, Object> response = restTemplate.getForObject(
                auditUrl + "/api/fraud/blacklist/" + ticketId, Map.class);
            return response != null && Boolean.TRUE.equals(response.get("blacklisted"));
        } catch (Exception e) {
            log.warn("Could not check blacklist for ticket {}: {}", ticketId, e.getMessage());
            return false;
        }
    }

    public void reportFraudAlert(Long ticketId, Long userId, String terminalId,
                                  String alertType, String description) {
        try {
            Map<String, Object> payload = Map.of(
                "ticketId", ticketId,
                "userId", userId != null ? userId : 0L,
                "terminalId", terminalId != null ? terminalId : "",
                "alertType", alertType,
                "description", description
            );
            restTemplate.postForObject(auditUrl + "/api/fraud/alerts", payload, Map.class);
        } catch (Exception e) {
            log.warn("Could not report fraud alert for ticket {}: {}", ticketId, e.getMessage());
        }
    }
}
