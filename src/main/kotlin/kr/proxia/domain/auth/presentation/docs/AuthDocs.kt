package kr.proxia.domain.auth.presentation.docs

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import kr.proxia.domain.auth.presentation.request.CheckEmailRequest
import kr.proxia.domain.auth.presentation.request.GithubLoginRequest
import kr.proxia.domain.auth.presentation.request.GoogleLoginRequest
import kr.proxia.domain.auth.presentation.request.LoginRequest
import kr.proxia.domain.auth.presentation.request.RegisterRequest
import kr.proxia.domain.auth.presentation.request.ReissueRequest
import kr.proxia.domain.auth.presentation.response.CheckEmailResponse
import kr.proxia.domain.auth.presentation.response.LoginResponse
import kr.proxia.domain.auth.presentation.response.ReissueResponse

@Tag(name = "Auth")
interface AuthDocs {
    @Operation(summary = "Google Login")
    fun googleLogin(request: GoogleLoginRequest): LoginResponse

    @Operation(summary = "Github Login")
    fun githubLogin(request: GithubLoginRequest): LoginResponse

    @Operation(summary = "Register")
    fun register(request: RegisterRequest)

    @Operation(summary = "Login")
    fun login(request: LoginRequest): LoginResponse

    @Operation(summary = "Check Email")
    fun checkEmail(request: CheckEmailRequest): CheckEmailResponse

    @Operation(summary = "Reissue")
    fun reissue(request: ReissueRequest): ReissueResponse

    @Operation(summary = "Logout")
    fun logout()
}