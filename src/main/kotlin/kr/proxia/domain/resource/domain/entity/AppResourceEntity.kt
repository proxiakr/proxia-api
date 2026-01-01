package kr.proxia.domain.resource.domain.entity

import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.Table
import kr.proxia.domain.service.domain.enums.AppFramework
import kr.proxia.global.jpa.common.BaseEntity
import java.util.UUID

@Entity
@Table(name = "app_resources")
class AppResourceEntity(
    val userId: UUID,
    framework: AppFramework?,
    repositoryUrl: String,
    branch: String?,
    rootDirectory: String?,
    buildCommand: String?,
    installCommand: String?,
    startCommand: String?,
    envVariables: String?,
) : BaseEntity() {
    @Enumerated(EnumType.STRING)
    var framework: AppFramework? = framework
        protected set

    var repositoryUrl: String = repositoryUrl
        protected set

    var branch: String? = branch
        protected set

    var rootDirectory: String? = rootDirectory
        protected set

    var buildCommand: String? = buildCommand
        protected set

    var installCommand: String? = installCommand
        protected set

    var startCommand: String? = startCommand
        protected set

    var envVariables: String? = envVariables
        protected set

    fun update(
        framework: AppFramework? = this.framework,
        repositoryUrl: String = this.repositoryUrl,
        branch: String? = this.branch,
        rootDirectory: String? = this.rootDirectory,
        buildCommand: String? = this.buildCommand,
        installCommand: String? = this.installCommand,
        startCommand: String? = this.startCommand,
        envVariables: String? = this.envVariables,
    ) {
        this.framework = framework
        this.repositoryUrl = repositoryUrl
        this.branch = branch
        this.rootDirectory = rootDirectory
        this.buildCommand = buildCommand
        this.installCommand = installCommand
        this.startCommand = startCommand
        this.envVariables = envVariables
    }
}
