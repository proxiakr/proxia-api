package kr.proxia.storage.db.core.entity

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.FetchType
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.Table
import kr.proxia.core.enums.DeploymentStatus
import java.time.LocalDateTime

@Entity
@Table(name = "deployments")
class Deployment(
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "app_service_id", nullable = false)
    val appService: AppService,
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    var status: DeploymentStatus = DeploymentStatus.QUEUED,
    @Column(nullable = false, length = 40)
    val commitSha: String,
    @Column(nullable = false, columnDefinition = "TEXT")
    val commitMessage: String,
    var startedAt: LocalDateTime? = null,
    var finishedAt: LocalDateTime? = null,
) : BaseEntity()
