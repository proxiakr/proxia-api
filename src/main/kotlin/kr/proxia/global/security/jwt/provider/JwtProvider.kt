package kr.proxia.global.security.jwt.provider

import io.jsonwebtoken.Jwts
import kr.proxia.domain.auth.domain.repository.RefreshTokenRepository
import kr.proxia.domain.user.domain.repository.UserRepository
import kr.proxia.global.security.jwt.enums.JwtType
import kr.proxia.global.security.jwt.properties.JwtProperties
import org.springframework.data.repository.findByIdOrNull
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.Authentication
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.stereotype.Component

@Component
class JwtProvider(
    private val jwtProperties: JwtProperties,
    private val refreshTokenRepository: RefreshTokenRepository,
    private val userRepository: UserRepository
) {
    fun getSubject(token: String): Long = Jwts.parser()
        .verifyWith(jwtProperties.secretKeySpec)
        .build()
        .parseSignedClaims(token)
        .payload
        .subject.toLong()

    fun getType(token: String): JwtType = JwtType.valueOf(
        Jwts.parser()
            .verifyWith(jwtProperties.secretKeySpec)
            .build()
            .parseSignedClaims(token)
            .header
            .type
    )

    fun getAuthentication(token: String): Authentication {
        val user = userRepository.findByIdOrNull(getSubject(token)) ?: throw IllegalArgumentException("User not found")

        return UsernamePasswordAuthenticationToken(user, null, listOf(SimpleGrantedAuthority("ROLE_${user.role.name}")))
    }

    fun createAccessToken(userId: Long): String {
        return Jwts.builder()
            .header()
            .type(JwtType.ACCESS.name)
            .and()
            .subject(userId.toString())
            .signWith(jwtProperties.secretKeySpec)
            .compact()
    }

    fun createRefreshToken(userId: Long): String {
        return Jwts.builder()
            .header()
            .type(JwtType.REFRESH.name)
            .and()
            .subject(userId.toString())
            .signWith(jwtProperties.secretKeySpec)
            .compact()
            .also {
                refreshTokenRepository.save(userId, it)
            }
    }
}