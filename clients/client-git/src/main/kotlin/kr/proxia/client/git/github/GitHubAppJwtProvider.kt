package kr.proxia.client.git.github

import io.jsonwebtoken.Jwts
import org.springframework.stereotype.Component
import java.security.KeyFactory
import java.security.PrivateKey
import java.security.spec.PKCS8EncodedKeySpec
import java.util.Base64
import java.util.Date

@Component
class GitHubAppJwtProvider {
    fun createAsyncJwt(
        appId: String,
        privateKeyPem: String,
    ): String {
        val now = System.currentTimeMillis()
        val issuedAt = Date(now - 60_000)
        val expiration = Date(now + 600_000)

        return Jwts
            .builder()
            .issuer(appId)
            .issuedAt(issuedAt)
            .expiration(expiration)
            .signWith(decodePrivateKey(privateKeyPem))
            .compact()
    }

    private fun decodePrivateKey(pem: String): PrivateKey {
        val cleanPem =
            pem
                .replace("-----BEGIN RSA PRIVATE KEY-----", "")
                .replace("-----END RSA PRIVATE KEY-----", "")
                .replace("-----BEGIN PRIVATE KEY-----", "")
                .replace("-----END PRIVATE KEY-----", "")
                .replace("\\s".toRegex(), "")

        val decoded = Base64.getDecoder().decode(cleanPem)
        val spec = PKCS8EncodedKeySpec(decoded)
        val kf = KeyFactory.getInstance("RSA")

        return kf.generatePrivate(spec)
    }
}
