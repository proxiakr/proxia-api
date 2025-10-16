package kr.proxia.global.security.holder

import kr.proxia.domain.user.domain.entity.UserEntity
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.stereotype.Component

@Component
class SecurityHolder {
    fun getUser(): UserEntity {
        val principal = SecurityContextHolder.getContext().authentication.principal ?: throw IllegalStateException("UserEntity not found in Security Context")

        return principal as UserEntity
    }

    fun isAuthenticated() = SecurityContextHolder.getContext().authentication.isAuthenticated
}