package kr.proxia.domain.project.domain.entity

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.Table
import kr.proxia.global.jpa.common.BaseEntity

@Entity
@Table(name = "projects")
class ProjectEntity(
    val userId: Long,
    val gitIntegrationId: Long,
    val name: String,
    val slug: String,
    val repositoryId: Long,
    var repositoryPath: String,
    var branch: String,
    @Enumerated(EnumType.STRING)
    @Column(columnDefinition = "VARCHAR")
    var framework: String,
    var buildCommand: String,
    var startCommand: String,
) : BaseEntity()