package kr.proxia.storage.db.core.entity

import jakarta.persistence.DiscriminatorColumn
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.FetchType
import jakarta.persistence.Inheritance
import jakarta.persistence.InheritanceType
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.Table
import kr.proxia.core.enums.ServiceStatus

@Entity
@Table(name = "services")
@DiscriminatorColumn(name = "type")
@Inheritance(strategy = InheritanceType.JOINED)
abstract class Service(
    val name: String,
    val x: Double,
    val y: Double,
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "project_id", nullable = false)
    val project: Project,
    @Enumerated(EnumType.STRING)
    var status: ServiceStatus,
) : BaseEntity()
