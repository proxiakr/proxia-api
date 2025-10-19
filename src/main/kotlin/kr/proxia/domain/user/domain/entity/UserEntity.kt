package kr.proxia.domain.user.domain.entity

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.Table
import kr.proxia.domain.user.domain.enums.OAuthProvider
import kr.proxia.domain.user.domain.enums.UserRole
import kr.proxia.global.jpa.common.BaseEntity

@Entity
@Table(name = "users")
class UserEntity(
    @Column(nullable = false)
    val name: String,

    @Column(nullable = false, unique = true)
    val email: String,

    password: String? = null,
    avatarUrl: String? = null,

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    val role: UserRole = UserRole.USER,

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    val provider: OAuthProvider,
    val providerId: String? = null
) : BaseEntity() {
    var password: String? = password
        protected set

    var avatarUrl: String? = avatarUrl
        protected set

    fun update(
        password: String? = this.password,
        avatarUrl: String? = this.avatarUrl,
    ) {
        this.password = password
        this.avatarUrl = avatarUrl
    }
}