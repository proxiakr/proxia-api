package kr.proxia.storage.db.core.entity

import jakarta.persistence.Column
import jakarta.persistence.Convert
import jakarta.persistence.DiscriminatorValue
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.Table
import kr.proxia.core.enums.DatabaseEngine
import kr.proxia.core.enums.ServiceStatus
import kr.proxia.storage.db.core.converter.EncryptConverter

@Entity
@Table(name = "database_services")
@DiscriminatorValue("DATABASE")
class DatabaseService(
    name: String,
    x: Double,
    y: Double,
    project: Project,
    status: ServiceStatus,
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    val engine: DatabaseEngine,
    @Column(nullable = false)
    val version: String,
    @Column(nullable = false)
    val database: String,
    @Column(nullable = false)
    val username: String,
    @Column(nullable = false)
    @Convert(converter = EncryptConverter::class)
    val password: String,
) : Service(name, x, y, project, status)
