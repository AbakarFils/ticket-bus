package com.ticketbus.service;

import com.ticketbus.entity.SigningKeyPair;
import com.ticketbus.exception.CryptoException;
import com.ticketbus.repository.SigningKeyPairRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.*;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.time.LocalDateTime;
import java.util.Base64;

@Service
@RequiredArgsConstructor
@Slf4j
public class CryptoService {

    private final SigningKeyPairRepository signingKeyPairRepository;

    @Transactional
    public SigningKeyPair generateKeyPair() {
        try {
            KeyPairGenerator generator = KeyPairGenerator.getInstance("RSA");
            generator.initialize(2048);
            KeyPair keyPair = generator.generateKeyPair();

            signingKeyPairRepository.findByActiveTrue().ifPresent(old -> {
                old.setActive(false);
                old.setRotatedAt(LocalDateTime.now());
                signingKeyPairRepository.save(old);
            });

            String publicKeyB64 = Base64.getEncoder().encodeToString(keyPair.getPublic().getEncoded());
            String privateKeyB64 = Base64.getEncoder().encodeToString(keyPair.getPrivate().getEncoded());

            SigningKeyPair newKeyPair = SigningKeyPair.builder()
                    .publicKey(publicKeyB64)
                    .privateKey(privateKeyB64)
                    .algorithm("RSA")
                    .keySize(2048)
                    .active(true)
                    .build();

            return signingKeyPairRepository.save(newKeyPair);
        } catch (NoSuchAlgorithmException e) {
            throw new CryptoException("Failed to generate key pair", e);
        }
    }

    public String signData(String data) {
        try {
            SigningKeyPair keyPair = getActiveKeyPair();
            byte[] privateKeyBytes = Base64.getDecoder().decode(keyPair.getPrivateKey());
            PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(privateKeyBytes);
            KeyFactory keyFactory = KeyFactory.getInstance("RSA");
            PrivateKey privateKey = keyFactory.generatePrivate(keySpec);

            Signature signature = Signature.getInstance("SHA256withRSA");
            signature.initSign(privateKey);
            signature.update(data.getBytes());
            return Base64.getEncoder().encodeToString(signature.sign());
        } catch (Exception e) {
            throw new CryptoException("Failed to sign data", e);
        }
    }

    public boolean verifySignature(String data, String signatureB64) {
        try {
            SigningKeyPair keyPair = getActiveKeyPair();
            byte[] publicKeyBytes = Base64.getDecoder().decode(keyPair.getPublicKey());
            X509EncodedKeySpec keySpec = new X509EncodedKeySpec(publicKeyBytes);
            KeyFactory keyFactory = KeyFactory.getInstance("RSA");
            PublicKey publicKey = keyFactory.generatePublic(keySpec);

            Signature signature = Signature.getInstance("SHA256withRSA");
            signature.initVerify(publicKey);
            signature.update(data.getBytes());
            return signature.verify(Base64.getDecoder().decode(signatureB64));
        } catch (Exception e) {
            log.warn("Signature verification failed: {}", e.getMessage());
            return false;
        }
    }

    public String getActivePublicKey() {
        return getActiveKeyPair().getPublicKey();
    }

    @Transactional
    public void rotateKeys() {
        generateKeyPair();
    }

    public SigningKeyPair getActiveKeyPair() {
        return signingKeyPairRepository.findByActiveTrue()
                .orElseThrow(() -> new CryptoException("No active signing key pair found"));
    }
}
