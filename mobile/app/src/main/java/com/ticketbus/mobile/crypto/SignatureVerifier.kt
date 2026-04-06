package com.ticketbus.mobile.crypto

import android.util.Base64
import android.util.Log
import com.google.gson.Gson
import com.google.gson.JsonObject
import com.ticketbus.mobile.util.PreferenceManager
import java.security.KeyFactory
import java.security.Signature
import java.security.spec.X509EncodedKeySpec
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

class SignatureVerifier(private val preferenceManager: PreferenceManager) {
    private val gson = Gson()
    private val tag = "SignatureVerifier"

    data class QrPayload(
        val ticketNumber: String,
        val nonce: String,
        val passenger: String?,
        val route: String?,
        val validFrom: String?,
        val validUntil: String?,
        val signature: String
    )

    fun parseAndVerify(qrJson: String): Pair<QrPayload?, Boolean> {
        return try {
            val obj = gson.fromJson(qrJson, JsonObject::class.java)
            val sig = obj.get("signature")?.asString ?: return Pair(null, false)
            val copy = obj.deepCopy() as JsonObject
            copy.remove("signature")
            val dataToVerify = gson.toJson(copy)
            val payload = QrPayload(
                ticketNumber = obj.get("ticketNumber")?.asString ?: return Pair(null, false),
                nonce = obj.get("nonce")?.asString ?: "",
                passenger = obj.get("passenger")?.asString,
                route = obj.get("route")?.asString,
                validFrom = obj.get("validFrom")?.asString,
                validUntil = obj.get("validUntil")?.asString,
                signature = sig
            )
            val verified = verifySignature(dataToVerify, sig)
            Pair(payload, verified)
        } catch (e: Exception) {
            Log.e(tag, "Parse error", e)
            Pair(null, false)
        }
    }

    private fun verifySignature(data: String, sigBase64: String): Boolean {
        val keyStr = preferenceManager.publicKey ?: return true
        return try {
            val cleaned = keyStr
                .replace("-----BEGIN PUBLIC KEY-----", "")
                .replace("-----END PUBLIC KEY-----", "")
                .replace("\\s".toRegex(), "")
            val keyBytes = Base64.decode(cleaned, Base64.NO_WRAP)
            val pubKey = KeyFactory.getInstance("RSA").generatePublic(X509EncodedKeySpec(keyBytes))
            val sig = Signature.getInstance("SHA256withRSA")
            sig.initVerify(pubKey)
            sig.update(data.toByteArray(Charsets.UTF_8))
            sig.verify(Base64.decode(sigBase64, Base64.NO_WRAP))
        } catch (e: Exception) {
            Log.e(tag, "Verification failed", e)
            false
        }
    }

    fun isExpired(validUntil: String?): Boolean {
        validUntil ?: return false
        return try {
            val exp = LocalDateTime.parse(validUntil, DateTimeFormatter.ISO_DATE_TIME)
            exp.isBefore(LocalDateTime.now())
        } catch (e: Exception) { false }
    }
}
