package kr.proxia.global.security.jwt.filter

import jakarta.servlet.FilterChain
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import kr.proxia.global.security.jwt.provider.JwtProvider
import kr.proxia.global.security.jwt.validator.JwtValidator
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.stereotype.Component
import org.springframework.web.filter.OncePerRequestFilter

@Component
class JwtAuthenticationFilter(private val jwtProvider: JwtProvider, private val jwtValidator: JwtValidator) : OncePerRequestFilter() {
    override fun doFilterInternal(
        request: HttpServletRequest,
        response: HttpServletResponse,
        filterChain: FilterChain
    ) {
        val token = request.getHeader("Authorization")?.removePrefix("Bearer ")

        if (token != null && jwtValidator.validateToken(token)) {
            val authentication = jwtProvider.getAuthentication(token)

            SecurityContextHolder.getContext().authentication = authentication
        }

        filterChain.doFilter(request, response)
    }
}