package kr.proxia.storage.db.core.entity

import jakarta.persistence.DiscriminatorValue
import jakarta.persistence.Entity
import jakarta.persistence.FetchType
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.Table
import kr.proxia.core.enums.ServiceStatus

@Entity
@Table(name = "app_services")
@DiscriminatorValue("APP")
class AppService(
    override val name: String,
    override val x: Double,
    override val y: Double,
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "project_id", nullable = false)
    override val project: Project,
    override val status: ServiceStatus,
    val branch: String?,
    val port: Int?,
) : Service(name, x, y, project, status)
