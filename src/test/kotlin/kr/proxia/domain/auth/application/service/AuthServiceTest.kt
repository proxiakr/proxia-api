package kr.proxia.domain.auth.application.service

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.shouldBe
import io.mockk.Runs
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.slot
import io.mockk.verify
import kr.proxia.domain.auth.domain.entity.RefreshTokenEntity
import kr.proxia.domain.auth.domain.error.AuthError
import kr.proxia.domain.auth.domain.repository.RefreshTokenRepository
import kr.proxia.domain.auth.presentation.request.CheckEmailRequest
import kr.proxia.domain.auth.presentation.request.GithubLoginRequest
import kr.proxia.domain.auth.presentation.request.GoogleLoginRequest
import kr.proxia.domain.auth.presentation.request.LoginRequest
import kr.proxia.domain.auth.presentation.request.RegisterRequest
import kr.proxia.domain.auth.presentation.request.ReissueRequest
import kr.proxia.domain.user.domain.entity.UserEntity
import kr.proxia.domain.user.domain.enums.OAuthProvider
import kr.proxia.domain.user.domain.enums.UserRole
import kr.proxia.domain.user.domain.error.UserError
import kr.proxia.domain.user.domain.repository.UserRepository
import kr.proxia.global.error.BusinessException
import kr.proxia.global.security.holder.SecurityHolder
import kr.proxia.global.security.jwt.extractor.JwtExtractor
import kr.proxia.global.security.jwt.properties.JwtProperties
import kr.proxia.global.security.jwt.provider.JwtProvider
import kr.proxia.global.security.jwt.validator.JwtValidator
import kr.proxia.global.security.oauth2.github.client.GithubOAuthClient
import kr.proxia.global.security.oauth2.github.data.GithubUserInfo
import kr.proxia.global.security.oauth2.google.client.GoogleOAuthClient
import kr.proxia.global.security.oauth2.google.data.GoogleUserInfo
import org.springframework.data.repository.findByIdOrNull
import org.springframework.security.crypto.password.PasswordEncoder
import java.util.UUID

class AuthServiceTest :
    BehaviorSpec({
        val googleOAuthClient = mockk<GoogleOAuthClient>()
        val githubOAuthClient = mockk<GithubOAuthClient>()
        val userRepository = mockk<UserRepository>()
        val refreshTokenRepository = mockk<RefreshTokenRepository>()
        val jwtValidator = mockk<JwtValidator>()
        val jwtProvider = mockk<JwtProvider>()
        val jwtExtractor = mockk<JwtExtractor>()
        val jwtProperties = mockk<JwtProperties>()
        val securityHolder = mockk<SecurityHolder>()
        val passwordEncoder = mockk<PasswordEncoder>()

        val authService =
            AuthService(
                googleOAuthClient,
                githubOAuthClient,
                userRepository,
                refreshTokenRepository,
                jwtValidator,
                jwtProvider,
                jwtExtractor,
                jwtProperties,
                securityHolder,
                passwordEncoder,
            )

        Given("googleLogin") {
            val request = GoogleLoginRequest(idToken = "valid-id-token")
            val googleUserInfo =
                GoogleUserInfo(
                    sub = "google-123",
                    email = "user@example.com",
                    name = "Test User",
                    picture = "https://example.com/avatar.jpg",
                )

            When("새로운 사용자가 로그인") {
                val userId = UUID.fromString("00000000-0000-0000-0000-000000000001")
                val savedUser =
                    mockk<UserEntity>(relaxed = true) {
                        every { id } returns userId
                        every { email } returns googleUserInfo.email
                        every { name } returns googleUserInfo.name
                        every { avatarUrl } returns googleUserInfo.picture
                        every { role } returns UserRole.USER
                    }
                val userSlot = slot<UserEntity>()

                every { googleOAuthClient.getUserInfo(request.idToken) } returns googleUserInfo
                every { userRepository.findByEmail(googleUserInfo.email) } returns null
                every { userRepository.save(capture(userSlot)) } returns savedUser
                every { jwtProperties.refreshTokenExpiration } returns 604800000L
                every { jwtProvider.createAccessToken(userId, UserRole.USER) } returns "access-token"
                every { jwtProvider.createRefreshToken(userId) } returns "refresh-token"
                every { refreshTokenRepository.save(any()) } returns mockk()

                val result = authService.googleLogin(request)

                Then("새 사용자 생성 및 토큰 발급") {
                    result.accessToken shouldBe "access-token"
                    result.refreshToken shouldBe "refresh-token"
                    result.user.email shouldBe googleUserInfo.email
                    result.user.name shouldBe googleUserInfo.name
                    result.user.avatarUrl shouldBe googleUserInfo.picture

                    userSlot.captured.email shouldBe googleUserInfo.email
                    userSlot.captured.provider shouldBe OAuthProvider.GOOGLE
                    userSlot.captured.providerId shouldBe googleUserInfo.sub
                }
            }

            When("기존 사용자가 로그인") {
                val userId = UUID.fromString("00000000-0000-0000-0000-000000000002")
                val existingUser =
                    mockk<UserEntity>(relaxed = true) {
                        every { id } returns userId
                        every { email } returns googleUserInfo.email
                        every { name } returns googleUserInfo.name
                        every { avatarUrl } returns googleUserInfo.picture
                        every { role } returns UserRole.USER
                    }

                every { googleOAuthClient.getUserInfo(request.idToken) } returns googleUserInfo
                every { userRepository.findByEmail(googleUserInfo.email) } returns existingUser
                every { jwtProperties.refreshTokenExpiration } returns 604800000L
                every { jwtProvider.createAccessToken(userId, UserRole.USER) } returns "access-token"
                every { jwtProvider.createRefreshToken(userId) } returns "refresh-token"
                every { refreshTokenRepository.save(any()) } returns mockk()

                val result = authService.googleLogin(request)

                Then("토큰 발급") {
                    result.accessToken shouldBe "access-token"
                    result.refreshToken shouldBe "refresh-token"
                    result.user.email shouldBe existingUser.email
                }
            }
        }

        Given("githubLogin") {
            val request = GithubLoginRequest(code = "auth-code")
            val githubUserInfo =
                GithubUserInfo(
                    id = 12345L,
                    login = "testuser",
                    name = "Test User",
                    email = "github@example.com",
                    avatarUrl = "https://github.com/avatar.jpg",
                )

            When("새로운 사용자가 로그인") {
                val userId = UUID.fromString("00000000-0000-0000-0000-000000000003")
                val savedUser =
                    mockk<UserEntity>(relaxed = true) {
                        every { id } returns userId
                        every { email } returns githubUserInfo.email
                        every { name } returns githubUserInfo.name!!
                        every { avatarUrl } returns githubUserInfo.avatarUrl
                        every { role } returns UserRole.USER
                    }
                val userSlot = slot<UserEntity>()

                every { githubOAuthClient.getUserInfo(request.code) } returns githubUserInfo
                every { userRepository.findByEmail(githubUserInfo.email) } returns null
                every { userRepository.save(capture(userSlot)) } returns savedUser
                every { jwtProperties.refreshTokenExpiration } returns 604800000L
                every { jwtProvider.createAccessToken(userId, UserRole.USER) } returns "access-token"
                every { jwtProvider.createRefreshToken(userId) } returns "refresh-token"
                every { refreshTokenRepository.save(any()) } returns mockk()

                val result = authService.githubLogin(request)

                Then("새 사용자 생성 및 토큰 발급") {
                    result.accessToken shouldBe "access-token"
                    result.refreshToken shouldBe "refresh-token"
                    result.user.email shouldBe githubUserInfo.email
                    result.user.name shouldBe githubUserInfo.name

                    userSlot.captured.email shouldBe githubUserInfo.email
                    userSlot.captured.provider shouldBe OAuthProvider.GITHUB
                    userSlot.captured.providerId shouldBe githubUserInfo.id.toString()
                }
            }

            When("이름이 null인 사용자가 로그인") {
                val userId = UUID.fromString("00000000-0000-0000-0000-000000000004")
                val userInfoWithoutName = githubUserInfo.copy(name = null)
                val savedUser =
                    mockk<UserEntity>(relaxed = true) {
                        every { id } returns userId
                        every { email } returns userInfoWithoutName.email
                        every { name } returns userInfoWithoutName.login
                        every { avatarUrl } returns userInfoWithoutName.avatarUrl
                        every { role } returns UserRole.USER
                    }
                val userSlot = slot<UserEntity>()

                every { githubOAuthClient.getUserInfo(request.code) } returns userInfoWithoutName
                every { userRepository.findByEmail(userInfoWithoutName.email) } returns null
                every { userRepository.save(capture(userSlot)) } returns savedUser
                every { jwtProperties.refreshTokenExpiration } returns 604800000L
                every { jwtProvider.createAccessToken(userId, UserRole.USER) } returns "access-token"
                every { jwtProvider.createRefreshToken(userId) } returns "refresh-token"
                every { refreshTokenRepository.save(any()) } returns mockk()

                authService.githubLogin(request)

                Then("login을 이름으로 사용") {
                    userSlot.captured.name shouldBe userInfoWithoutName.login
                }
            }

            When("기존 사용자가 로그인") {
                val userId = UUID.fromString("00000000-0000-0000-0000-000000000005")
                val existingUser =
                    mockk<UserEntity>(relaxed = true) {
                        every { id } returns userId
                        every { email } returns githubUserInfo.email
                        every { name } returns (githubUserInfo.name ?: githubUserInfo.login)
                        every { avatarUrl } returns githubUserInfo.avatarUrl
                        every { role } returns UserRole.USER
                    }

                every { githubOAuthClient.getUserInfo(request.code) } returns githubUserInfo
                every { userRepository.findByEmail(githubUserInfo.email) } returns existingUser
                every { jwtProperties.refreshTokenExpiration } returns 604800000L
                every { jwtProvider.createAccessToken(userId, UserRole.USER) } returns "access-token"
                every { jwtProvider.createRefreshToken(userId) } returns "refresh-token"
                every { refreshTokenRepository.save(any()) } returns mockk()

                val result = authService.githubLogin(request)

                Then("토큰 발급") {
                    result.accessToken shouldBe "access-token"
                    result.refreshToken shouldBe "refresh-token"
                }
            }
        }

        Given("register") {
            val request =
                RegisterRequest(
                    email = "newuser@example.com",
                    name = "New User",
                    password = "password123",
                )

            When("유효한 요청") {
                val userSlot = slot<UserEntity>()
                every { userRepository.existsByEmail(request.email) } returns false
                every { passwordEncoder.encode(request.password) } returns "encoded-password"
                every { userRepository.save(capture(userSlot)) } returns mockk()

                authService.register(request)

                Then("사용자 생성") {
                    userSlot.captured.email shouldBe request.email
                    userSlot.captured.name shouldBe request.name
                    userSlot.captured.password shouldBe "encoded-password"
                    userSlot.captured.provider shouldBe OAuthProvider.LOCAL
                }
            }

            When("이미 존재하는 이메일") {
                every { userRepository.existsByEmail(request.email) } returns true

                Then("예외 발생") {
                    val exception =
                        shouldThrow<BusinessException> {
                            authService.register(request)
                        }
                    exception.error shouldBe UserError.EmailAlreadyExists
                }
            }
        }

        Given("login") {
            val request =
                LoginRequest(
                    email = "user@example.com",
                    password = "password123",
                )

            When("유효한 로그인") {
                val userId = UUID.fromString("00000000-0000-0000-0000-000000000006")
                val user =
                    mockk<UserEntity>(relaxed = true) {
                        every { id } returns userId
                        every { email } returns request.email
                        every { name } returns "Test User"
                        every { password } returns "encoded-password"
                        every { provider } returns OAuthProvider.LOCAL
                        every { role } returns UserRole.USER
                        every { avatarUrl } returns null
                    }

                every { userRepository.findByEmail(request.email) } returns user
                every { passwordEncoder.matches(request.password, "encoded-password") } returns true
                every { jwtProperties.refreshTokenExpiration } returns 604800000L
                every { jwtProvider.createAccessToken(userId, UserRole.USER) } returns "access-token"
                every { jwtProvider.createRefreshToken(userId) } returns "refresh-token"
                every { refreshTokenRepository.save(any()) } returns mockk()

                val result = authService.login(request)

                Then("토큰 발급") {
                    result.accessToken shouldBe "access-token"
                    result.refreshToken shouldBe "refresh-token"
                    result.user.email shouldBe user.email
                }
            }

            When("존재하지 않는 사용자") {
                every { userRepository.findByEmail(request.email) } returns null

                Then("예외 발생") {
                    val exception =
                        shouldThrow<BusinessException> {
                            authService.login(request)
                        }
                    exception.error shouldBe UserError.NotFound
                }
            }

            When("OAuth 제공자로 등록된 사용자") {
                val user =
                    mockk<UserEntity>(relaxed = true) {
                        every { email } returns request.email
                        every { provider } returns OAuthProvider.GOOGLE
                    }

                every { userRepository.findByEmail(request.email) } returns user

                Then("예외 발생") {
                    val exception =
                        shouldThrow<BusinessException> {
                            authService.login(request)
                        }
                    exception.error shouldBe UserError.InvalidOAuthProvider("GOOGLE")
                }
            }

            When("잘못된 비밀번호") {
                val user =
                    mockk<UserEntity>(relaxed = true) {
                        every { email } returns request.email
                        every { password } returns "encoded-password"
                        every { provider } returns OAuthProvider.LOCAL
                    }

                every { userRepository.findByEmail(request.email) } returns user
                every { passwordEncoder.matches(request.password, "encoded-password") } returns false

                Then("예외 발생") {
                    val exception =
                        shouldThrow<BusinessException> {
                            authService.login(request)
                        }
                    exception.error shouldBe AuthError.InvalidPassword
                }
            }
        }

        Given("checkEmail") {
            val request = CheckEmailRequest(email = "test@example.com")

            When("이메일이 존재함") {
                every { userRepository.existsByEmail(request.email) } returns true

                val result = authService.checkEmail(request)

                Then("exists가 true") {
                    result.exists shouldBe true
                }
            }

            When("이메일이 존재하지 않음") {
                every { userRepository.existsByEmail(request.email) } returns false

                val result = authService.checkEmail(request)

                Then("exists가 false") {
                    result.exists shouldBe false
                }
            }
        }

        Given("reissue") {
            val request = ReissueRequest(refreshToken = "old-refresh-token")
            val userId = UUID.fromString("00000000-0000-0000-0000-000000000007")

            When("유효한 리프레시 토큰") {
                val user =
                    mockk<UserEntity>(relaxed = true) {
                        every { id } returns userId
                        every { role } returns UserRole.USER
                    }
                val refreshTokenEntity = mockk<RefreshTokenEntity>(relaxed = true)

                every { jwtValidator.validateRefreshToken(request.refreshToken) } just Runs
                every { jwtExtractor.getSubject(request.refreshToken) } returns userId
                every { userRepository.findByIdOrNull(userId) } returns user
                every {
                    refreshTokenRepository.findByUserIdAndRefreshToken(
                        userId,
                        request.refreshToken,
                    )
                } returns refreshTokenEntity
                every { jwtProvider.createRefreshToken(userId) } returns "new-refresh-token"
                every { jwtProvider.createAccessToken(userId, UserRole.USER) } returns "new-access-token"
                every { refreshTokenEntity.update(refreshToken = "new-refresh-token") } just Runs

                val result = authService.reissue(request)

                Then("새 토큰 발급") {
                    result.accessToken shouldBe "new-access-token"
                    result.refreshToken shouldBe "new-refresh-token"
                    verify { refreshTokenEntity.update(refreshToken = "new-refresh-token") }
                }
            }

            When("사용자를 찾을 수 없음") {
                every { jwtValidator.validateRefreshToken(request.refreshToken) } just Runs
                every { jwtExtractor.getSubject(request.refreshToken) } returns userId
                every { userRepository.findByIdOrNull(userId) } returns null

                Then("예외 발생") {
                    val exception =
                        shouldThrow<BusinessException> {
                            authService.reissue(request)
                        }
                    exception.error shouldBe UserError.NotFound
                }
            }

            When("리프레시 토큰을 찾을 수 없음") {
                val user =
                    mockk<UserEntity>(relaxed = true) {
                        every { id } returns userId
                    }

                every { jwtValidator.validateRefreshToken(request.refreshToken) } just Runs
                every { jwtExtractor.getSubject(request.refreshToken) } returns userId
                every { userRepository.findByIdOrNull(userId) } returns user
                every { refreshTokenRepository.findByUserIdAndRefreshToken(userId, request.refreshToken) } returns null

                Then("예외 발생") {
                    val exception =
                        shouldThrow<BusinessException> {
                            authService.reissue(request)
                        }
                    exception.error shouldBe AuthError.RefreshTokenNotFound
                }
            }
        }

        Given("logout") {
            val userId = UUID.fromString("00000000-0000-0000-0000-000000000008")

            When("유효한 사용자") {
                every { securityHolder.getUserId() } returns userId
                every { userRepository.existsById(userId) } returns true
                every { refreshTokenRepository.deleteByUserId(userId) } just Runs

                authService.logout()

                Then("리프레시 토큰 삭제") {
                    verify { refreshTokenRepository.deleteByUserId(userId) }
                }
            }

            When("존재하지 않는 사용자") {
                every { securityHolder.getUserId() } returns userId
                every { userRepository.existsById(userId) } returns false

                Then("예외 발생") {
                    val exception =
                        shouldThrow<BusinessException> {
                            authService.logout()
                        }
                    exception.error shouldBe UserError.NotFound
                }
            }
        }
    })
