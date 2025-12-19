package kr.proxia.global.security.encryption

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties(prefix = "encryption")
data class EncryptionProperties(
    val secretKey: String,
)