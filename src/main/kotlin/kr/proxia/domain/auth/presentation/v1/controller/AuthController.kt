package kr.proxia.domain.auth.presentation.v1.controller

import kr.proxia.domain.auth.application.service.AuthService
import kr.proxia.domain.auth.presentation.v1.docs.AuthDocs
import kr.proxia.domain.auth.presentation.v1.request.GithubLoginRequest
import kr.proxia.domain.auth.presentation.v1.request.GoogleLoginRequest
import kr.proxia.domain.auth.presentation.v1.request.LoginRequest
import kr.proxia.domain.auth.presentation.v1.request.RegisterRequest
import kr.proxia.domain.auth.presentation.v1.request.ReissueRequest
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/v1/auth")
class AuthController(private val authService: AuthService) : AuthDocs {
    @PostMapping("/login/google")
    override fun googleLogin(@RequestBody request: GoogleLoginRequest) = authService.googleLogin(request)

    @PostMapping("/login/github")
    override fun githubLogin(@RequestBody request: GithubLoginRequest) = authService.githubLogin(request)

    @PostMapping("/register")
    override fun register(@RequestBody request: RegisterRequest) = authService.register(request)

    @PostMapping("/login")
    override fun login(@RequestBody request: LoginRequest) = authService.login(request)

    @PostMapping("/reissue")
    override fun reissue(@RequestBody request: ReissueRequest) = authService.reissue(request)

    @PostMapping("/logout")
    override fun logout() = authService.logout()
}