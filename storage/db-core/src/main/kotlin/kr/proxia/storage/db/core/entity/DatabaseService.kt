package kr.proxia.storage.db.core.entity

import jakarta.persistence.DiscriminatorValue
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.FetchType
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.Table
import kr.proxia.core.enums.DatabaseType
import kr.proxia.core.enums.ServiceStatus

@Entity
@Table(name = "database_services")
@DiscriminatorValue("DATABASE")
class DatabaseService(
    override val name: String,
    override val x: Double,
    override val y: Double,
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "project_id", nullable = false)
    override val project: Project,
    override val status: ServiceStatus,
    @Enumerated(EnumType.STRING)
    val databaseType: DatabaseType,
    val version: String,
    val username: String,
    val password: String,
) : Service(name, x, y, project, status)
