package kr.proxia.global.jpa.common

import com.github.f4b6a3.ulid.UlidCreator
import jakarta.persistence.Column
import jakarta.persistence.EntityListeners
import jakarta.persistence.Id
import jakarta.persistence.MappedSuperclass
import org.hibernate.proxy.HibernateProxy
import org.springframework.data.annotation.CreatedDate
import org.springframework.data.annotation.LastModifiedDate
import org.springframework.data.domain.Persistable
import org.springframework.data.jpa.domain.support.AuditingEntityListener
import java.time.LocalDateTime
import java.util.Objects
import java.util.UUID

@MappedSuperclass
@EntityListeners(AuditingEntityListener::class)
abstract class BaseEntity : Persistable<UUID> {
    @Id
    @Column(columnDefinition = "uuid")
    private val id: UUID = UlidCreator.getMonotonicUlid().toUuid()

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    lateinit var createdAt: LocalDateTime
        protected set

    @LastModifiedDate
    @Column(name = "updated_at", nullable = false)
    lateinit var updatedAt: LocalDateTime
        protected set

    var deletedAt: LocalDateTime? = null
        protected set

    @Transient
    private var isNew = true

    fun activate() {
        deletedAt = null
    }

    fun delete() {
        deletedAt = LocalDateTime.now()
    }

    val isDeleted: Boolean
        get() = deletedAt != null

    override fun getId(): UUID = id

    override fun isNew(): Boolean = isNew

    override fun equals(other: Any?): Boolean {
        other ?: return false
        if (other !is HibernateProxy && this::class != other::class) return false

        return id ==
            when (other) {
                is HibernateProxy -> other.hibernateLazyInitializer.identifier
                else -> (other as BaseEntity).id
            }
    }

    override fun hashCode() = Objects.hashCode(id)
}
