package kr.proxia.domain.auth.presentation.controller

import kr.proxia.domain.auth.application.service.AuthService
import kr.proxia.domain.auth.presentation.docs.AuthDocs
import kr.proxia.domain.auth.presentation.request.CheckEmailRequest
import kr.proxia.domain.auth.presentation.request.GithubLoginRequest
import kr.proxia.domain.auth.presentation.request.GoogleLoginRequest
import kr.proxia.domain.auth.presentation.request.LoginRequest
import kr.proxia.domain.auth.presentation.request.RegisterRequest
import kr.proxia.domain.auth.presentation.request.ReissueRequest
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/auth")
class AuthController(
    private val authService: AuthService,
) : AuthDocs {
    @PostMapping("/login/google")
    override fun googleLogin(
        @RequestBody request: GoogleLoginRequest,
    ) = authService.googleLogin(request)

    @PostMapping("/login/github")
    override fun githubLogin(
        @RequestBody request: GithubLoginRequest,
    ) = authService.githubLogin(request)

    @PostMapping("/register")
    override fun register(
        @RequestBody request: RegisterRequest,
    ) = authService.register(request)

    @PostMapping("/login")
    override fun login(
        @RequestBody request: LoginRequest,
    ) = authService.login(request)

    @PostMapping("/check-email")
    override fun checkEmail(
        @RequestBody request: CheckEmailRequest,
    ) = authService.checkEmail(request)

    @PostMapping("/reissue")
    override fun reissue(
        @RequestBody request: ReissueRequest,
    ) = authService.reissue(request)

    @PostMapping("/logout")
    override fun logout() = authService.logout()
}
