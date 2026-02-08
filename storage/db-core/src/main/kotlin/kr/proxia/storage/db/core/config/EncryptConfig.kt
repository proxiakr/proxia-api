package kr.proxia.storage.db.core.config

import jakarta.annotation.PostConstruct
import kr.proxia.storage.db.core.converter.EncryptConverter
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Configuration
import java.security.MessageDigest
import javax.crypto.spec.SecretKeySpec

@Configuration
class EncryptConfig(
    @Value("\${encrypt.key}") private val encryptKey: String,
) {
    @PostConstruct
    fun init() {
        val keyBytes = MessageDigest.getInstance("SHA-256").digest(encryptKey.toByteArray())
        EncryptConverter.key = SecretKeySpec(keyBytes, "AES")
    }
}
