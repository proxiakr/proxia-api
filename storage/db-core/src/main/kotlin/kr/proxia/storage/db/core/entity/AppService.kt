package kr.proxia.storage.db.core.entity

import jakarta.persistence.Column
import jakarta.persistence.DiscriminatorValue
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.FetchType
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.Table
import kr.proxia.core.enums.Framework
import kr.proxia.core.enums.ServiceStatus

@Entity
@Table(name = "app_services")
@DiscriminatorValue("APP")
class AppService(
    name: String,
    x: Double,
    y: Double,
    project: Project,
    status: ServiceStatus,
    @Column(nullable = false)
    val repoFullName: String,
    @Column(nullable = false)
    val branch: String,
    @Column(nullable = false)
    val port: Int,
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    val framework: Framework,
    val rootDirectory: String? = null,
    val buildCommand: String? = null,
    val startCommand: String? = null,
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "git_connection_id", nullable = false)
    val gitConnection: GitConnection,
) : Service(name, x, y, project, status)
