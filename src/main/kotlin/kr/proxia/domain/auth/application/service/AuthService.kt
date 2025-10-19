package kr.proxia.domain.auth.application.service

import kr.proxia.domain.auth.domain.repository.RefreshTokenRepository
import kr.proxia.domain.auth.presentation.v1.request.GithubLoginRequest
import kr.proxia.domain.auth.presentation.v1.request.GoogleLoginRequest
import kr.proxia.domain.auth.presentation.v1.request.LoginRequest
import kr.proxia.domain.auth.presentation.v1.request.RegisterRequest
import kr.proxia.domain.auth.presentation.v1.request.ReissueRequest
import kr.proxia.domain.auth.presentation.v1.response.LoginResponse
import kr.proxia.domain.auth.presentation.v1.response.ReissueResponse
import kr.proxia.domain.user.domain.entity.UserEntity
import kr.proxia.domain.user.domain.enums.OAuthProvider
import kr.proxia.domain.user.domain.repository.UserRepository
import kr.proxia.global.security.holder.SecurityHolder
import kr.proxia.global.security.jwt.enums.JwtType
import kr.proxia.global.security.jwt.provider.JwtProvider
import kr.proxia.global.security.jwt.validator.JwtValidator
import kr.proxia.global.security.oauth2.github.client.GithubOAuthClient
import kr.proxia.global.security.oauth2.google.client.GoogleOAuthClient
import org.springframework.data.repository.findByIdOrNull
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service

@Service
class AuthService(
    private val jwtProvider: JwtProvider,
    private val userRepository: UserRepository,
    private val googleOAuthClient: GoogleOAuthClient,
    private val githubOAuthClient: GithubOAuthClient,
    private val securityHolder: SecurityHolder,
    private val refreshTokenRepository: RefreshTokenRepository,
    private val jwtValidator: JwtValidator,
    private val passwordEncoder: PasswordEncoder
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
                    providerId = userInfo.sub
                )
            )

        return LoginResponse.of(user)
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
                    providerId = userInfo.id.toString()
                )
            )

        return LoginResponse.of(user)
    }

    fun register(request: RegisterRequest): LoginResponse {
        if (userRepository.existsByEmail(request.email))
            throw IllegalArgumentException("Email already exists")

        val user = userRepository.save(
            UserEntity(
                email = request.email,
                name = request.name,
                password = passwordEncoder.encode(request.password),
                provider = OAuthProvider.LOCAL
            )
        )

        return LoginResponse.of(user)
    }

    fun login(request: LoginRequest): LoginResponse {
        val user = userRepository.findByEmail(request.email)
            ?: throw IllegalArgumentException("User not found")

        if (user.provider != OAuthProvider.LOCAL)
            throw IllegalArgumentException("User is registered with ${user.provider} provider")

        if (!passwordEncoder.matches(request.password, user.password))
            throw IllegalArgumentException("Invalid password")


        return LoginResponse.of(user)
    }

    fun reissue(request: ReissueRequest): ReissueResponse {
        val refreshToken = request.refreshToken

        if (!jwtValidator.validateToken(refreshToken))
            throw IllegalArgumentException("Invalid token")

        if (jwtProvider.getType(refreshToken) != JwtType.REFRESH)
            throw IllegalArgumentException("Invalid token type")

        val userId = jwtProvider.getSubject(refreshToken)
        val user = userRepository.findByIdOrNull(userId)
            ?: throw IllegalArgumentException("User not found")

        if (refreshTokenRepository.findByUserId(userId) != refreshToken)
            throw IllegalArgumentException("Refresh token not found")

        return ReissueResponse(
            accessToken = jwtProvider.createAccessToken(user),
            refreshToken = jwtProvider.createRefreshToken(user)
        )
    }

    fun logout() {
        val userId = securityHolder.getUserId()

        if (!userRepository.existsById(userId))
            throw IllegalArgumentException("User not found")

        refreshTokenRepository.deleteByUserId(userId)
    }

    private fun LoginResponse.Companion.of(user: UserEntity) = LoginResponse(
        accessToken = jwtProvider.createAccessToken(user),
        refreshToken = jwtProvider.createRefreshToken(user),
        user = LoginResponse.User(
            id = user.id,
            email = user.email,
            name = user.name,
            avatarUrl = user.avatarUrl
        )
    )
}