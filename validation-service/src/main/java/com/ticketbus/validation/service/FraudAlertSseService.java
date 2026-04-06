package com.ticketbus.validation.service;

import com.ticketbus.common.domain.FraudAlert;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Server-Sent Events service for real-time fraud alert broadcasting.
 */
@Slf4j
@Service
public class FraudAlertSseService {

    private final List<SseEmitter> emitters = new CopyOnWriteArrayList<>();

    public SseEmitter subscribe() {
        SseEmitter emitter = new SseEmitter(30 * 60 * 1000L); // 30 minutes timeout
        emitters.add(emitter);
        emitter.onCompletion(() -> emitters.remove(emitter));
        emitter.onTimeout(() -> emitters.remove(emitter));
        emitter.onError(e -> emitters.remove(emitter));
        log.info("SSE client subscribed. Active subscribers: {}", emitters.size());
        return emitter;
    }

    public void publish(FraudAlert alert) {
        log.info("Broadcasting fraud alert #{} to {} subscribers", alert.getId(), emitters.size());
        for (SseEmitter emitter : emitters) {
            try {
                emitter.send(SseEmitter.event()
                    .name("fraud-alert")
                    .data(alert));
            } catch (IOException e) {
                emitters.remove(emitter);
            }
        }
    }

    public int getSubscriberCount() {
        return emitters.size();
    }
}

