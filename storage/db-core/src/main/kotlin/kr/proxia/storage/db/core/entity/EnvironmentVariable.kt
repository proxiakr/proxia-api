package kr.proxia.storage.db.core.entity

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.FetchType
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.Table
import jakarta.persistence.UniqueConstraint

@Entity
@Table(
    name = "environment_variables",
    uniqueConstraints = [UniqueConstraint(columnNames = ["service_id", "key"])],
)
class EnvironmentVariable(
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "service_id", nullable = false)
    val service: Service,
    @Column(name = "`key`", nullable = false)
    val key: String,
    @Column(nullable = false, columnDefinition = "TEXT")
    var value: String,
    @Column(nullable = false)
    val isSecret: Boolean = false,
) : BaseEntity()
