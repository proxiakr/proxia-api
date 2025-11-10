package kr.proxia.global.security.encryption

import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import java.util.Base64
import javax.crypto.Cipher
import javax.crypto.spec.SecretKeySpec

@Component
class EncryptionService(
    @Value("\${encryption.secret-key}") private val secretKey: String,
) {
    private val algorithm = "AES"
    private val transformation = "AES/ECB/PKCS5Padding"

    fun encrypt(value: String?): String? {
        if (value == null) return null
        val cipher = Cipher.getInstance(transformation)
        val keySpec = SecretKeySpec(secretKey.toByteArray().copyOf(16), algorithm)
        cipher.init(Cipher.ENCRYPT_MODE, keySpec)
        val encrypted = cipher.doFinal(value.toByteArray())
        return Base64.getEncoder().encodeToString(encrypted)
    }

    fun decrypt(encryptedValue: String?): String? {
        if (encryptedValue == null) return null
        val cipher = Cipher.getInstance(transformation)
        val keySpec = SecretKeySpec(secretKey.toByteArray().copyOf(16), algorithm)
        cipher.init(Cipher.DECRYPT_MODE, keySpec)
        val decoded = Base64.getDecoder().decode(encryptedValue)
        val decrypted = cipher.doFinal(decoded)
        return String(decrypted)
    }
}
