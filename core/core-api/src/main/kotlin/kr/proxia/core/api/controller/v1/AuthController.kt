package kr.proxia.core.api.controller.v1

import jakarta.validation.Valid
import kr.proxia.client.oauth.github.GitHubOAuthClient
import kr.proxia.client.oauth.google.GoogleOAuthClient
import kr.proxia.core.api.controller.v1.request.OAuthRequest
import kr.proxia.core.api.controller.v1.request.RefreshRequest
import kr.proxia.core.domain.auth.AuthService
import kr.proxia.core.domain.auth.TokenPair
import kr.proxia.core.enums.AuthProvider
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/v1/auth")
class AuthController(
    private val authService: AuthService,
    private val googleOAuthClient: GoogleOAuthClient,
    private val gitHubOAuthClient: GitHubOAuthClient,
) {
    @PostMapping("/google")
    fun google(
        @Valid @RequestBody request: OAuthRequest,
    ): TokenPair {
        val tokenResponse = googleOAuthClient.getAccessToken(request.code)
        val userInfo = googleOAuthClient.getUserInfo(tokenResponse.accessToken)

        return authService.authenticateOAuth(
            provider = AuthProvider.GOOGLE,
            providerId = userInfo.id,
            email = userInfo.email,
        )
    }

    @PostMapping("/github")
    fun github(
        @Valid @RequestBody request: OAuthRequest,
    ): TokenPair {
        val tokenResponse = gitHubOAuthClient.getAccessToken(request.code)
        val userInfo = gitHubOAuthClient.getUserInfo(tokenResponse.accessToken)

        val email = userInfo.email ?: run {
            val emails = gitHubOAuthClient.getUserEmails(tokenResponse.accessToken)
            emails.firstOrNull { it.primary && it.verified }?.email
                ?: emails.firstOrNull { it.verified }?.email
                ?: throw IllegalArgumentException("No verified email found")
        }

        return authService.authenticateOAuth(
            provider = AuthProvider.GITHUB,
            providerId = userInfo.id.toString(),
            email = email,
        )
    }

    @PostMapping("/refresh")
    fun refresh(
        @Valid @RequestBody request: RefreshRequest,
    ): TokenPair = authService.refresh(request.refreshToken)

    @PostMapping("/logout")
    fun logout(
        @Valid @RequestBody request: RefreshRequest,
    ) = authService.logout(request.refreshToken)
}
