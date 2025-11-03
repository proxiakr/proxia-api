package kr.proxia.domain.auth.application.service

import kr.proxia.domain.auth.domain.entity.RefreshTokenEntity
import kr.proxia.domain.auth.domain.repository.RefreshTokenRepository
import kr.proxia.domain.auth.presentation.v1.request.CheckEmailRequest
import kr.proxia.domain.auth.presentation.v1.request.GithubLoginRequest
import kr.proxia.domain.auth.presentation.v1.request.GoogleLoginRequest
import kr.proxia.domain.auth.presentation.v1.request.LoginRequest
import kr.proxia.domain.auth.presentation.v1.request.RegisterRequest
import kr.proxia.domain.auth.presentation.v1.request.ReissueRequest
import kr.proxia.domain.auth.presentation.v1.response.CheckEmailResponse
import kr.proxia.domain.auth.presentation.v1.response.LoginResponse
import kr.proxia.domain.auth.presentation.v1.response.ReissueResponse
import kr.proxia.domain.user.domain.entity.UserEntity
import kr.proxia.domain.user.domain.enums.OAuthProvider
import kr.proxia.domain.user.domain.repository.UserRepository
import kr.proxia.global.security.holder.SecurityHolder
import kr.proxia.global.security.jwt.extractor.JwtExtractor
import kr.proxia.global.security.jwt.properties.JwtProperties
import kr.proxia.global.security.jwt.provider.JwtProvider
import kr.proxia.global.security.jwt.validator.JwtValidator
import kr.proxia.global.security.oauth2.github.client.GithubOAuthClient
import kr.proxia.global.security.oauth2.google.client.GoogleOAuthClient
import org.springframework.data.repository.findByIdOrNull
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service
import java.time.LocalDateTime
import java.time.temporal.ChronoUnit

@Service
class AuthService(
    private val googleOAuthClient: GoogleOAuthClient,
    private val githubOAuthClient: GithubOAuthClient,
    private val userRepository: UserRepository,
    private val refreshTokenRepository: RefreshTokenRepository,
    private val jwtValidator: JwtValidator,
    private val jwtProvider: JwtProvider,
    private val jwtExtractor: JwtExtractor,
    private val jwtProperties: JwtProperties,
    private val securityHolder: SecurityHolder,
    private val passwordEncoder: PasswordEncoder,
) {
    fun googleLogin(request: GoogleLoginRequest): LoginResponse {
        val userInfo = googleOAuthClient.getUserInfo(request.idToken)
        val user = userRepository.findByEmail(userInfo.email)
            ?: userRepository.save(
                UserEntity(
                    email = userInfo.email,
                    name = userInfo.name,
                    avatarUrl = userInfo.picture,
                    provider = OAuthProvider.GOOGLE,
                    providerId = userInfo.sub,
                )
            )

        return generateLoginResponse(user)
    }

    fun githubLogin(request: GithubLoginRequest): LoginResponse {
        val userInfo = githubOAuthClient.getUserInfo(request.code)
        val user = userRepository.findByEmail(userInfo.email)
            ?: userRepository.save(
                UserEntity(
                    email = userInfo.email,
                    name = userInfo.name ?: userInfo.login,
                    avatarUrl = userInfo.avatarUrl,
                    provider = OAuthProvider.GITHUB,
                    providerId = userInfo.id.toString(),
                )
            )

        return generateLoginResponse(user)
    }

    fun register(request: RegisterRequest) {
        if (userRepository.existsByEmail(request.email))
            throw IllegalArgumentException("Email already exists")

        /**
         * TODO: Validates
         */

        userRepository.save(
            UserEntity(
                email = request.email,
                name = request.name,
                password = passwordEncoder.encode(request.password),
                provider = OAuthProvider.LOCAL,
            )
        )
    }

    fun login(request: LoginRequest): LoginResponse {
        val user = userRepository.findByEmail(request.email)
            ?: throw IllegalArgumentException("User not found")

        if (user.provider != OAuthProvider.LOCAL)
            throw IllegalArgumentException("User is registered with ${user.provider} provider")

        if (!passwordEncoder.matches(request.password, user.password))
            throw IllegalArgumentException("Invalid password")

        return generateLoginResponse(user)
    }

    fun checkEmail(request: CheckEmailRequest): CheckEmailResponse {
        val exists = userRepository.existsByEmail(request.email)

        return CheckEmailResponse(exists = exists)
    }

    fun reissue(request: ReissueRequest): ReissueResponse {
        jwtValidator.validateRefreshToken(request.refreshToken)

        val userId = jwtExtractor.getSubject(request.refreshToken)
        val user = userRepository.findByIdOrNull(userId)
            ?: throw IllegalArgumentException("User not found")

        val refreshToken = refreshTokenRepository.findByUserIdAndRefreshToken(user.id, request.refreshToken)
            ?: throw IllegalArgumentException("Refresh token not found")

        val newRefreshToken = jwtProvider.createRefreshToken(user.id)
        refreshToken.update(refreshToken = newRefreshToken)

        return ReissueResponse(
            accessToken = jwtProvider.createAccessToken(user.id, user.role),
            refreshToken = newRefreshToken,
        )
    }

    fun logout() {
        val userId = securityHolder.getUserId()

        if (!userRepository.existsById(userId))
            throw IllegalArgumentException("User not found")

        refreshTokenRepository.deleteByUserId(userId)
    }

    private fun generateToken(user: UserEntity): Pair<String, String> {
        val accessToken = jwtProvider.createAccessToken(user.id, user.role)
        val refreshToken = jwtProvider.createRefreshToken(user.id)

        refreshTokenRepository.save(
            RefreshTokenEntity(
                userId = user.id,
                refreshToken = refreshToken,
                expiresAt = LocalDateTime.now().plus(jwtProperties.refreshTokenExpiration, ChronoUnit.MILLIS),
            )
        )

        return accessToken to refreshToken
    }

    private fun generateLoginResponse(user: UserEntity): LoginResponse {
        val (accessToken, refreshToken) = generateToken(user)

        return LoginResponse(
            accessToken = accessToken,
            refreshToken = refreshToken,
            user = LoginResponse.User(
                id = user.id,
                email = user.email,
                name = user.name,
                avatarUrl = user.avatarUrl,
            ),
        )
    }
}