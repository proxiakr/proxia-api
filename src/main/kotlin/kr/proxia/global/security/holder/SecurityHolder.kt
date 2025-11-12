package kr.proxia.global.security.holder

import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.stereotype.Component
import java.util.UUID

@Component
class SecurityHolder {
    fun getUserId(): UUID {
        val principal =
            SecurityContextHolder.getContext().authentication.principal
                ?: throw IllegalStateException("Authentication principal is null")

        return principal as? UUID ?: throw IllegalStateException("Authentication principal is null")
    }

    fun isAuthenticated() = SecurityContextHolder.getContext().authentication.isAuthenticated
}
