package kr.proxia.domain.resource.domain.entity

import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.Table
import kr.proxia.domain.service.domain.enums.DatabaseType
import kr.proxia.global.jpa.common.BaseEntity
import java.util.UUID

@Entity
@Table(name = "database_resources")
class DatabaseResourceEntity(
    val userId: UUID,
    type: DatabaseType,
    database: String?,
    username: String?,
    password: String?,
) : BaseEntity() {
    @Enumerated(EnumType.STRING)
    var type: DatabaseType = type
        protected set

    var database: String? = database
        protected set

    var username: String? = username
        protected set

    var password: String? = password
        protected set

    fun update(
        type: DatabaseType = this.type,
        database: String? = this.database,
        username: String? = this.username,
        password: String? = this.password,
    ) {
        this.type = type
        this.database = database
        this.username = username
        this.password = password
    }
}
