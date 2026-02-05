package kr.proxia.core.api.controller.v1

import io.mockk.every
import io.mockk.mockk
import kr.proxia.client.oauth.github.GitHubOAuthClient
import kr.proxia.client.oauth.github.GitHubTokenResponse
import kr.proxia.client.oauth.github.GitHubUserInfo
import kr.proxia.client.oauth.google.GoogleOAuthClient
import kr.proxia.client.oauth.google.GoogleTokenResponse
import kr.proxia.client.oauth.google.GoogleUserInfo
import kr.proxia.core.api.controller.v1.request.OAuthRequest
import kr.proxia.core.api.controller.v1.request.RefreshRequest
import kr.proxia.core.domain.AuthService
import kr.proxia.core.domain.TokenPair
import kr.proxia.core.enums.AuthProvider
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.http.MediaType
import org.springframework.restdocs.RestDocumentationContextProvider
import org.springframework.restdocs.RestDocumentationExtension
import org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document
import org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.documentationConfiguration
import org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.post
import org.springframework.restdocs.operation.preprocess.Preprocessors.preprocessRequest
import org.springframework.restdocs.operation.preprocess.Preprocessors.preprocessResponse
import org.springframework.restdocs.operation.preprocess.Preprocessors.prettyPrint
import org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath
import org.springframework.restdocs.payload.PayloadDocumentation.requestFields
import org.springframework.restdocs.payload.PayloadDocumentation.responseFields
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import org.springframework.test.web.servlet.setup.MockMvcBuilders
import org.springframework.test.web.servlet.setup.StandaloneMockMvcBuilder
import tools.jackson.databind.ObjectMapper
import tools.jackson.module.kotlin.jsonMapper
import tools.jackson.module.kotlin.kotlinModule

@Tag("restdocs")
@ExtendWith(RestDocumentationExtension::class)
class AuthControllerDocsTest {
    private lateinit var mockMvc: MockMvc
    private val objectMapper: ObjectMapper = jsonMapper { addModule(kotlinModule()) }

    private val authService = mockk<AuthService>()
    private val googleOAuthClient = mockk<GoogleOAuthClient>()
    private val gitHubOAuthClient = mockk<GitHubOAuthClient>()

    @BeforeEach
    fun setUp(restDocumentation: RestDocumentationContextProvider) {
        mockMvc =
            MockMvcBuilders
                .standaloneSetup(AuthController(authService, googleOAuthClient, gitHubOAuthClient))
                .apply<StandaloneMockMvcBuilder>(documentationConfiguration(restDocumentation))
                .build()
    }

    @Test
    fun `Google OAuth 로그인`() {
        val request = OAuthRequest(code = "google-auth-code")
        val tokenPair =
            TokenPair(
                accessToken = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
                refreshToken = "refresh-token-value",
            )

        every { googleOAuthClient.getAccessToken("google-auth-code") } returns
            GoogleTokenResponse(
                accessToken = "google-access-token",
                expiresIn = 3600,
                tokenType = "Bearer",
                scope = "email profile",
            )
        every { googleOAuthClient.getUserInfo("google-access-token") } returns
            GoogleUserInfo(
                id = "google-user-id",
                email = "user@gmail.com",
                verifiedEmail = true,
                name = "Test User",
                picture = null,
            )
        every {
            authService.authenticateOAuth(AuthProvider.GOOGLE, "google-user-id", "user@gmail.com")
        } returns tokenPair

        mockMvc
            .perform(
                post("/api/v1/auth/google")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)),
            ).andExpect(status().isOk)
            .andDo(
                document(
                    "auth-google",
                    preprocessRequest(prettyPrint()),
                    preprocessResponse(prettyPrint()),
                    requestFields(
                        fieldWithPath("code").description("Google OAuth authorization code"),
                    ),
                    responseFields(
                        fieldWithPath("accessToken").description("JWT access token"),
                        fieldWithPath("refreshToken").description("Refresh token for token renewal"),
                    ),
                ),
            )
    }

    @Test
    fun `GitHub OAuth 로그인`() {
        val request = OAuthRequest(code = "github-auth-code")
        val tokenPair =
            TokenPair(
                accessToken = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
                refreshToken = "refresh-token-value",
            )

        every { gitHubOAuthClient.getAccessToken("github-auth-code") } returns
            GitHubTokenResponse(
                accessToken = "github-access-token",
                tokenType = "bearer",
                scope = "user:email",
            )
        every { gitHubOAuthClient.getUserInfo("github-access-token") } returns
            GitHubUserInfo(
                id = 12345678,
                login = "testuser",
                email = "user@github.com",
                name = "Test User",
                avatarUrl = null,
            )
        every {
            authService.authenticateOAuth(AuthProvider.GITHUB, "12345678", "user@github.com")
        } returns tokenPair

        mockMvc
            .perform(
                post("/api/v1/auth/github")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)),
            ).andExpect(status().isOk)
            .andDo(
                document(
                    "auth-github",
                    preprocessRequest(prettyPrint()),
                    preprocessResponse(prettyPrint()),
                    requestFields(
                        fieldWithPath("code").description("GitHub OAuth authorization code"),
                    ),
                    responseFields(
                        fieldWithPath("accessToken").description("JWT access token"),
                        fieldWithPath("refreshToken").description("Refresh token for token renewal"),
                    ),
                ),
            )
    }

    @Test
    fun `토큰 갱신`() {
        val request = RefreshRequest(refreshToken = "current-refresh-token")
        val tokenPair =
            TokenPair(
                accessToken = "new-access-token",
                refreshToken = "new-refresh-token",
            )

        every { authService.refresh("current-refresh-token") } returns tokenPair

        mockMvc
            .perform(
                post("/api/v1/auth/refresh")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)),
            ).andExpect(status().isOk)
            .andDo(
                document(
                    "auth-refresh",
                    preprocessRequest(prettyPrint()),
                    preprocessResponse(prettyPrint()),
                    requestFields(
                        fieldWithPath("refreshToken").description("Current refresh token"),
                    ),
                    responseFields(
                        fieldWithPath("accessToken").description("New JWT access token"),
                        fieldWithPath("refreshToken").description("New refresh token (rotation)"),
                    ),
                ),
            )
    }

    @Test
    fun `로그아웃`() {
        val request = RefreshRequest(refreshToken = "refresh-token-to-invalidate")

        every { authService.logout("refresh-token-to-invalidate") } returns Unit

        mockMvc
            .perform(
                post("/api/v1/auth/logout")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)),
            ).andExpect(status().isOk)
            .andDo(
                document(
                    "auth-logout",
                    preprocessRequest(prettyPrint()),
                    preprocessResponse(prettyPrint()),
                    requestFields(
                        fieldWithPath("refreshToken").description("Refresh token to invalidate"),
                    ),
                ),
            )
    }
}
