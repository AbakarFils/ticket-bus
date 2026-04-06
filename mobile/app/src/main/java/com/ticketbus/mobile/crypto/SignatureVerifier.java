package com.ticketbus.mobile.crypto;

import android.util.Base64;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.ticketbus.mobile.util.PreferenceManager;

import java.security.KeyFactory;
import java.security.PublicKey;
import java.security.Signature;
import java.security.spec.X509EncodedKeySpec;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class SignatureVerifier {
    private static final String TAG = "SignatureVerifier";
    private final PreferenceManager preferenceManager;
    private final Gson gson = new Gson();

    public static class QrPayload {
        public final String ticketNumber;
        public final String nonce;
        public final String passenger;
        public final String route;
        public final String validFrom;
        public final String validUntil;
        public final String signature;

        public QrPayload(String ticketNumber, String nonce, String passenger,
                         String route, String validFrom, String validUntil, String signature) {
            this.ticketNumber = ticketNumber;
            this.nonce = nonce;
            this.passenger = passenger;
            this.route = route;
            this.validFrom = validFrom;
            this.validUntil = validUntil;
            this.signature = signature;
        }
    }

    public static class ParseResult {
        public final QrPayload payload;
        public final boolean signatureValid;

        public ParseResult(QrPayload payload, boolean signatureValid) {
            this.payload = payload;
            this.signatureValid = signatureValid;
        }
    }

    public SignatureVerifier(PreferenceManager preferenceManager) {
        this.preferenceManager = preferenceManager;
    }

    public ParseResult parseAndVerify(String qrJson) {
        try {
            JsonObject obj = gson.fromJson(qrJson, JsonObject.class);
            if (!obj.has("signature") || !obj.has("ticketNumber")) {
                return new ParseResult(null, false);
            }
            String sig = obj.get("signature").getAsString();
            JsonObject copy = obj.deepCopy();
            copy.remove("signature");
            String dataToVerify = gson.toJson(copy);

            QrPayload payload = new QrPayload(
                obj.get("ticketNumber").getAsString(),
                obj.has("nonce") ? obj.get("nonce").getAsString() : "",
                obj.has("passenger") ? obj.get("passenger").getAsString() : null,
                obj.has("route") ? obj.get("route").getAsString() : null,
                obj.has("validFrom") ? obj.get("validFrom").getAsString() : null,
                obj.has("validUntil") ? obj.get("validUntil").getAsString() : null,
                sig
            );
            boolean verified = verifySignature(dataToVerify, sig);
            return new ParseResult(payload, verified);
        } catch (Exception e) {
            Log.e(TAG, "Parse error", e);
            return new ParseResult(null, false);
        }
    }

    private boolean verifySignature(String data, String sigBase64) {
        String keyStr = preferenceManager.getPublicKey();
        if (keyStr == null) return true; // no cached key yet → allow through
        try {
            String cleaned = keyStr
                .replace("-----BEGIN PUBLIC KEY-----", "")
                .replace("-----END PUBLIC KEY-----", "")
                .replaceAll("\\s", "");
            byte[] keyBytes = Base64.decode(cleaned, Base64.NO_WRAP);
            PublicKey pubKey = KeyFactory.getInstance("RSA")
                .generatePublic(new X509EncodedKeySpec(keyBytes));
            Signature sig = Signature.getInstance("SHA256withRSA");
            sig.initVerify(pubKey);
            sig.update(data.getBytes(java.nio.charset.StandardCharsets.UTF_8));
            return sig.verify(Base64.decode(sigBase64, Base64.NO_WRAP));
        } catch (Exception e) {
            Log.e(TAG, "Verification failed", e);
            return false;
        }
    }

    public boolean isExpired(String validUntil) {
        if (validUntil == null) return false;
        try {
            LocalDateTime exp = LocalDateTime.parse(validUntil, DateTimeFormatter.ISO_DATE_TIME);
            return exp.isBefore(LocalDateTime.now());
        } catch (Exception e) {
            return false;
        }
    }
}
