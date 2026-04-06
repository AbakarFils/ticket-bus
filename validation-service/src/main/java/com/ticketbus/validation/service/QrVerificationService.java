package com.ticketbus.validation.service;

import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.security.*;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;

@Slf4j
@Service
public class QrVerificationService {

    @Value("${validation.rsa.public-key:}")
    private String publicKeyBase64;

    private PublicKey publicKey;

    @PostConstruct
    public void init() throws Exception {
        if (publicKeyBase64 != null && !publicKeyBase64.isBlank()) {
            KeyFactory kf = KeyFactory.getInstance("RSA");
            byte[] pubBytes = Base64.getDecoder().decode(publicKeyBase64.trim());
            publicKey = kf.generatePublic(new X509EncodedKeySpec(pubBytes));
            log.info("RSA public key loaded from configuration");
        } else {
            KeyPairGenerator kpg = KeyPairGenerator.getInstance("RSA");
            kpg.initialize(2048);
            KeyPair kp = kpg.generateKeyPair();
            publicKey = kp.getPublic();
            log.warn("No RSA public key configured - generated ephemeral key for dev (signatures from ticketing-service will not verify)");
        }
    }

    public boolean verifySignature(String payload, String signatureBase64) {
        try {
            Signature sig = Signature.getInstance("SHA256withRSA");
            sig.initVerify(publicKey);
            sig.update(payload.getBytes(java.nio.charset.StandardCharsets.UTF_8));
            return sig.verify(Base64.getDecoder().decode(signatureBase64));
        } catch (Exception e) {
            log.error("Signature verification error", e);
            return false;
        }
    }
}
