package kr.proxia.domain.auth.application.service

import kr.proxia.domain.auth.domain.repository.RefreshTokenRepository
import kr.proxia.domain.auth.presentation.dto.request.GithubLoginRequest
import kr.proxia.domain.auth.presentation.dto.request.GoogleLoginRequest
import kr.proxia.domain.auth.presentation.dto.request.LoginRequest
import kr.proxia.domain.auth.presentation.dto.request.RegisterRequest
import kr.proxia.domain.auth.presentation.dto.request.ReissueRequest
import kr.proxia.domain.auth.presentation.dto.response.LoginResponse
import kr.proxia.domain.auth.presentation.dto.response.ReissueResponse
import kr.proxia.domain.user.domain.entity.UserEntity
import kr.proxia.domain.user.domain.enums.OAuthProvider
import kr.proxia.domain.user.domain.repository.UserRepository
import kr.proxia.global.security.holder.SecurityHolder
import kr.proxia.global.security.jwt.enums.JwtType
import kr.proxia.global.security.jwt.provider.JwtProvider
import kr.proxia.global.security.jwt.validator.JwtValidator
import kr.proxia.global.security.oauth2.github.client.GithubOAuthClient
import kr.proxia.global.security.oauth2.google.client.GoogleOAuthClient
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
        val userId = user.id!!

        return LoginResponse(
            accessToken = jwtProvider.createAccessToken(userId),
            refreshToken = jwtProvider.createRefreshToken(userId),
            user = LoginResponse.User(
                id = userId,
                email = user.email,
                name = user.name,
                avatarUrl = user.avatarUrl
            )
        )
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
        val userId = user.id!!

        return LoginResponse(
            accessToken = jwtProvider.createAccessToken(userId),
            refreshToken = jwtProvider.createRefreshToken(userId),
            user = LoginResponse.User(
                id = userId,
                email = user.email,
                name = user.name,
                avatarUrl = user.avatarUrl
            )
        )
    }

    fun register(request: RegisterRequest): LoginResponse {
        if (userRepository.existsByEmail(request.email))
            throw IllegalArgumentException("Email already exists")

        val user = userRepository.save(UserEntity(
            email = request.email,
            name = request.name,
            password = passwordEncoder.encode(request.password),
            provider = OAuthProvider.LOCAL
        ))
        val userId = user.id!!

        return LoginResponse(
            accessToken = jwtProvider.createAccessToken(userId),
            refreshToken = jwtProvider.createRefreshToken(userId),
            user = LoginResponse.User(
                id = userId,
                email = user.email,
                name = user.name,
                avatarUrl = user.avatarUrl
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

        val userId = user.id!!

        return LoginResponse(
            accessToken = jwtProvider.createAccessToken(userId),
            refreshToken = jwtProvider.createRefreshToken(userId),
            user = LoginResponse.User(
                id = userId,
                email = user.email,
                name = user.name,
                avatarUrl = user.avatarUrl
            )
        )
    }

    fun reissue(request: ReissueRequest): ReissueResponse {
        val refreshToken = request.refreshToken

        if (!jwtValidator.validateToken(refreshToken))
            throw IllegalArgumentException("Invalid token")

        if (jwtProvider.getType(refreshToken) != JwtType.REFRESH)
            throw IllegalArgumentException("Invalid token type")

        val userId = jwtProvider.getSubject(refreshToken)

        if (!userRepository.existsById(userId))
            throw IllegalArgumentException("User not found")

        if (refreshTokenRepository.findByUserId(userId) != refreshToken)
            throw IllegalArgumentException("Refresh token not found")

        return ReissueResponse(
            accessToken = jwtProvider.createAccessToken(userId),
            refreshToken = jwtProvider.createRefreshToken(userId)
        )
    }

    fun logout() {
        val user = securityHolder.getUser()

        refreshTokenRepository.deleteByUserId(user.id!!)
    }
}