package kr.proxia.global.security.jwt.properties

import io.jsonwebtoken.Jwts
import org.springframework.boot.context.properties.ConfigurationProperties
import javax.crypto.spec.SecretKeySpec

@ConfigurationProperties(prefix = "jwt")
data class JwtProperties(
    val secretKey: String,
    val accessTokenExpiration: Long,
    val refreshTokenExpiration: Long,
) {
    val secretKeySpec = SecretKeySpec(secretKey.toByteArray(), Jwts.SIG.HS256.key().build().algorithm)
}