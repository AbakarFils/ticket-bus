package com.ticketbus.ticketing.service;

import com.ticketbus.common.domain.Ticket;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.security.*;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;

@Slf4j
@Service
public class QrSigningService {

    @Value("${ticketing.rsa.private-key:}")
    private String privateKeyBase64;

    @Value("${ticketing.rsa.public-key:}")
    private String publicKeyBase64;

    private PrivateKey privateKey;
    private PublicKey publicKey;

    @PostConstruct
    public void init() throws Exception {
        if (privateKeyBase64 != null && !privateKeyBase64.isBlank()
                && publicKeyBase64 != null && !publicKeyBase64.isBlank()) {
            KeyFactory kf = KeyFactory.getInstance("RSA");
            byte[] privBytes = Base64.getDecoder().decode(privateKeyBase64.trim());
            byte[] pubBytes = Base64.getDecoder().decode(publicKeyBase64.trim());
            privateKey = kf.generatePrivate(new PKCS8EncodedKeySpec(privBytes));
            publicKey = kf.generatePublic(new X509EncodedKeySpec(pubBytes));
            log.info("RSA keys loaded from configuration");
        } else {
            KeyPairGenerator kpg = KeyPairGenerator.getInstance("RSA");
            kpg.initialize(2048);
            KeyPair kp = kpg.generateKeyPair();
            privateKey = kp.getPrivate();
            publicKey = kp.getPublic();
            String pubBase64 = Base64.getEncoder().encodeToString(publicKey.getEncoded());
            log.warn("No RSA keys configured - generated ephemeral key pair for dev. Public key (X509/base64): {}", pubBase64);
        }
    }

    public String getPublicKeyBase64() {
        return Base64.getEncoder().encodeToString(publicKey.getEncoded());
    }

    public String buildPayload(Ticket ticket) {
        return ticket.getId() + "|" +
               ticket.getUserId() + "|" +
               ticket.getNonce() + "|" +
               ticket.getValidFrom() + "|" +
               ticket.getValidUntil() + "|" +
               ticket.getMaxUsage();
    }

    public String sign(String payload) throws Exception {
        Signature sig = Signature.getInstance("SHA256withRSA");
        sig.initSign(privateKey);
        sig.update(payload.getBytes(java.nio.charset.StandardCharsets.UTF_8));
        return Base64.getEncoder().encodeToString(sig.sign());
    }

    public boolean verify(String payload, String signatureBase64) {
        try {
            Signature sig = Signature.getInstance("SHA256withRSA");
            sig.initVerify(publicKey);
            sig.update(payload.getBytes(java.nio.charset.StandardCharsets.UTF_8));
            return sig.verify(Base64.getDecoder().decode(signatureBase64));
        } catch (Exception e) {
            log.error("Signature verification failed", e);
            return false;
        }
    }
}
