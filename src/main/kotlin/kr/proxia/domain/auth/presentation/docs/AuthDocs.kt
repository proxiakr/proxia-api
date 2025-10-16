package kr.proxia.domain.auth.presentation.docs

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import kr.proxia.domain.auth.presentation.dto.request.GithubLoginRequest
import kr.proxia.domain.auth.presentation.dto.request.GoogleLoginRequest
import kr.proxia.domain.auth.presentation.dto.request.LoginRequest
import kr.proxia.domain.auth.presentation.dto.request.RegisterRequest
import kr.proxia.domain.auth.presentation.dto.request.ReissueRequest
import kr.proxia.domain.auth.presentation.dto.response.LoginResponse
import kr.proxia.domain.auth.presentation.dto.response.ReissueResponse

@Tag(name = "Auth")
interface AuthDocs {
    @Operation(summary = "Google Login")
    fun googleLogin(request: GoogleLoginRequest): LoginResponse

    @Operation(summary = "Github Login")
    fun githubLogin(request: GithubLoginRequest): LoginResponse

    @Operation(summary = "Register")
    fun register(request: RegisterRequest): LoginResponse

    @Operation(summary = "Login")
    fun login(request: LoginRequest): LoginResponse

    @Operation(summary = "Reissue")
    fun reissue(request: ReissueRequest): ReissueResponse

    @Operation(summary = "Logout")
    fun logout()
}