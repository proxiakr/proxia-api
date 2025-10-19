package kr.proxia.global.security.holder

import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.stereotype.Component

@Component
class SecurityHolder {
    fun getUserId(): Long {
        val principal = SecurityContextHolder.getContext().authentication.principal ?: throw IllegalStateException("Authentication principal is null")

        return principal as Long
    }

    fun isAuthenticated() = SecurityContextHolder.getContext().authentication.isAuthenticated
}