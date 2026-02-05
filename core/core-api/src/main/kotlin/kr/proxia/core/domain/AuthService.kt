package kr.proxia.core.domain

import kr.proxia.core.enums.AuthProvider
import kr.proxia.core.support.error.CoreException
import kr.proxia.core.support.error.ErrorType
import kr.proxia.storage.db.core.entity.RefreshToken
import kr.proxia.storage.db.core.entity.User
import kr.proxia.storage.db.core.repository.RefreshTokenRepository
import kr.proxia.storage.db.core.repository.UserRepository
import kr.proxia.support.security.JwtProperties
import kr.proxia.support.security.JwtProvider
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime

@Service
class AuthService(
    private val userRepository: UserRepository,
    private val refreshTokenRepository: RefreshTokenRepository,
    private val jwtProvider: JwtProvider,
    private val jwtProperties: JwtProperties,
    private val workspaceService: WorkspaceService,
) {
    @Transactional
    fun authenticateOAuth(
        provider: AuthProvider,
        providerId: String,
        email: String,
        name: String,
    ): TokenPair {
        val existingUser = userRepository.findByProviderAndProviderId(provider, providerId)

        val user =
            existingUser ?: run {
                val newUser =
                    userRepository.save(
                        User(
                            email = email,
                            name = name,
                            provider = provider,
                            providerId = providerId,
                        ),
                    )
                workspaceService.createDefaultWorkspace(newUser)
                newUser
            }

        return createTokenPair(user)
    }

    @Transactional
    fun refresh(token: String): TokenPair {
        val refreshToken =
            refreshTokenRepository.findByToken(token)
                ?: throw CoreException(ErrorType.INVALID_TOKEN)

        if (refreshToken.expiresAt.isBefore(LocalDateTime.now())) {
            refreshTokenRepository.delete(refreshToken)
            throw CoreException(ErrorType.TOKEN_EXPIRED)
        }

        refreshTokenRepository.delete(refreshToken)

        return createTokenPair(refreshToken.user)
    }

    @Transactional
    fun logout(token: String) {
        val refreshToken = refreshTokenRepository.findByToken(token) ?: return
        refreshTokenRepository.delete(refreshToken)
    }

    private fun createTokenPair(user: User): TokenPair {
        val accessToken = jwtProvider.createAccessToken(user.id, user.role.name)
        val refreshToken = jwtProvider.createRefreshToken(user.id)

        val expiresAt =
            LocalDateTime
                .now()
                .plusSeconds(jwtProperties.refreshExpiration / 1000)

        refreshTokenRepository.save(
            RefreshToken(
                user = user,
                token = refreshToken,
                expiresAt = expiresAt,
            ),
        )

        return TokenPair(
            accessToken = accessToken,
            refreshToken = refreshToken,
        )
    }
}
