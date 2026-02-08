package kr.proxia.storage.db.core.entity

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.FetchType
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.Table
import kr.proxia.core.enums.DeploymentStage
import kr.proxia.core.enums.DeploymentStageStatus
import java.time.LocalDateTime

@Entity
@Table(name = "deployment_logs")
class DeploymentLog(
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "deployment_id", nullable = false)
    val deployment: Deployment,
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    val stage: DeploymentStage,
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    var status: DeploymentStageStatus = DeploymentStageStatus.RUNNING,
    var startedAt: LocalDateTime? = null,
    var finishedAt: LocalDateTime? = null,
    var logUrl: String? = null,
) : BaseEntity()
