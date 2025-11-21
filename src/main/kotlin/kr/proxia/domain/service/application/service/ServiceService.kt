package kr.proxia.domain.service.application.service

import kr.proxia.domain.connection.domain.repository.ConnectionRepository
import kr.proxia.domain.project.domain.error.ProjectError
import kr.proxia.domain.project.domain.repository.ProjectRepository
import kr.proxia.domain.resource.domain.entity.AppResourceEntity
import kr.proxia.domain.resource.domain.entity.DatabaseResourceEntity
import kr.proxia.domain.resource.domain.entity.DomainResourceEntity
import kr.proxia.domain.resource.domain.repository.AppResourceRepository
import kr.proxia.domain.resource.domain.repository.DatabaseResourceRepository
import kr.proxia.domain.resource.domain.repository.DomainResourceRepository
import kr.proxia.domain.resource.presentation.response.AppResourceResponse
import kr.proxia.domain.resource.presentation.response.DatabaseResourceResponse
import kr.proxia.domain.resource.presentation.response.DomainResourceResponse
import kr.proxia.domain.service.domain.entity.ServiceEntity
import kr.proxia.domain.service.domain.enums.ServiceType
import kr.proxia.domain.service.domain.error.ServiceError
import kr.proxia.domain.service.domain.repository.ServiceRepository
import kr.proxia.domain.service.presentation.request.CreateServiceRequest
import kr.proxia.domain.service.presentation.request.UpdateServicePositionRequest
import kr.proxia.domain.service.presentation.request.UpdateServiceRequest
import kr.proxia.domain.service.presentation.response.ServiceResponse
import kr.proxia.global.error.BusinessException
import kr.proxia.global.security.encryption.EncryptionService
import kr.proxia.global.security.holder.SecurityHolder
import org.springframework.context.ApplicationEventPublisher
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.UUID

@Service
class ServiceService(
    private val serviceRepository: ServiceRepository,
    private val connectionRepository: ConnectionRepository,
    private val projectRepository: ProjectRepository,
    private val appResourceRepository: AppResourceRepository,
    private val databaseResourceRepository: DatabaseResourceRepository,
    private val domainResourceRepository: DomainResourceRepository,
    private val encryptionService: EncryptionService,
    private val securityHolder: SecurityHolder,
    private val eventPublisher: ApplicationEventPublisher,
) {
    @Transactional
    fun createService(
        projectId: UUID,
        request: CreateServiceRequest,
    ) {
        val userId = securityHolder.getUserId()
        validateProjectAccess(projectId, userId)

        val targetId =
            when (request.type) {
                ServiceType.APP ->
                    request.appResource?.let {
                        appResourceRepository
                            .save(
                                AppResourceEntity(
                                    userId = userId,
                                    framework = it.framework,
                                    repositoryUrl = it.repositoryUrl,
                                    branch = it.branch,
                                    rootDirectory = it.rootDirectory,
                                    buildCommand = it.buildCommand,
                                    installCommand = it.installCommand,
                                    startCommand = it.startCommand,
                                    envVariables = it.envVariables,
                                ),
                            ).id
                    }

                ServiceType.DATABASE ->
                    request.databaseResource?.let {
                        databaseResourceRepository
                            .save(
                                DatabaseResourceEntity(
                                    userId = userId,
                                    type = it.type,
                                    database = it.database,
                                    username = it.username,
                                    password = encryptionService.encrypt(it.password),
                                ),
                            ).id
                    }

                ServiceType.DOMAIN ->
                    request.domainResource?.let {
                        domainResourceRepository
                            .save(
                                DomainResourceEntity(
                                    userId = userId,
                                    subdomain = it.subdomain,
                                    customDomain = it.customDomain,
                                ),
                            ).id
                    }

                else -> null
            }

        serviceRepository.save(
            ServiceEntity(
                projectId = projectId,
                userId = userId,
                name = request.name,
                description = request.description,
                type = request.type,
                x = request.x,
                y = request.y,
                targetId = targetId,
            ),
        )

        eventPublisher.publishEvent()
    }

    fun getServices(projectId: UUID): List<ServiceResponse> {
        val userId = securityHolder.getUserId()
        validateProjectAccess(projectId, userId)

        val services = serviceRepository.findAllByProjectIdAndDeletedAtIsNull(projectId)

        val appResourceIds =
            services.filter { it.type == ServiceType.APP && it.targetId != null }.mapNotNull { it.targetId }
        val databaseResourceIds =
            services.filter { it.type == ServiceType.DATABASE && it.targetId != null }.mapNotNull { it.targetId }
        val domainResourceIds =
            services.filter { it.type == ServiceType.DOMAIN && it.targetId != null }.mapNotNull { it.targetId }

        val appResources: Map<UUID, AppResourceEntity> =
            if (appResourceIds.isNotEmpty()) {
                appResourceRepository.findAllById(appResourceIds).associateBy { it.id }
            } else {
                emptyMap()
            }

        val databaseResources: Map<UUID, DatabaseResourceEntity> =
            if (databaseResourceIds.isNotEmpty()) {
                databaseResourceRepository.findAllById(databaseResourceIds).associateBy { it.id }
            } else {
                emptyMap()
            }

        val domainResources: Map<UUID, DomainResourceEntity> =
            if (domainResourceIds.isNotEmpty()) {
                domainResourceRepository.findAllById(domainResourceIds).associateBy { it.id }
            } else {
                emptyMap()
            }

        return services.map { service ->
            val appResource = service.targetId?.let { appResources[it] }?.let { AppResourceResponse.of(it) }
            val databaseResource =
                service.targetId?.let { databaseResources[it] }?.let { DatabaseResourceResponse.of(it) }
            val domainResource = service.targetId?.let { domainResources[it] }?.let { DomainResourceResponse.of(it) }
            ServiceResponse.of(service, appResource, databaseResource, domainResource)
        }
    }

    fun getService(serviceId: UUID): ServiceResponse {
        val userId = securityHolder.getUserId()
        val service = serviceRepository.findByIdAndDeletedAtIsNull(serviceId)
            ?: throw BusinessException(ServiceError.SERVICE_NOT_FOUND)

        if (service.userId != userId) {
            throw BusinessException(ServiceError.SERVICE_ACCESS_DENIED)
        }

        val targetId = service.targetId
        val appResource =
            if (service.type == ServiceType.APP && targetId != null) {
                appResourceRepository.findByIdOrNull(targetId)?.let { AppResourceResponse.of(it) }
            } else {
                null
            }

        val databaseResource =
            if (service.type == ServiceType.DATABASE && targetId != null) {
                databaseResourceRepository.findByIdOrNull(targetId)?.let { DatabaseResourceResponse.of(it) }
            } else {
                null
            }

        val domainResource =
            if (service.type == ServiceType.DOMAIN && targetId != null) {
                domainResourceRepository.findByIdOrNull(targetId)?.let { DomainResourceResponse.of(it) }
            } else {
                null
            }

        return ServiceResponse.of(service, appResource, databaseResource, domainResource)
    }

    @Transactional
    fun updateService(
        serviceId: UUID,
        request: UpdateServiceRequest,
    ) {
        val userId = securityHolder.getUserId()
        val service = serviceRepository.findByIdAndDeletedAtIsNull(serviceId)
            ?: throw BusinessException(ServiceError.SERVICE_NOT_FOUND)

        if (service.userId != userId) {
            throw BusinessException(ServiceError.SERVICE_ACCESS_DENIED)
        }

        val targetId =
            when (request.type) {
                ServiceType.APP ->
                    request.appResource?.let {
                        val currentTargetId = service.targetId
                        if (currentTargetId != null) {
                            appResourceRepository.findByIdOrNull(currentTargetId)?.update(
                                framework = it.framework,
                                repositoryUrl = it.repositoryUrl,
                                branch = it.branch,
                                rootDirectory = it.rootDirectory,
                                buildCommand = it.buildCommand,
                                installCommand = it.installCommand,
                                startCommand = it.startCommand,
                                envVariables = it.envVariables,
                            )
                            currentTargetId
                        } else {
                            appResourceRepository
                                .save(
                                    AppResourceEntity(
                                        userId = userId,
                                        framework = it.framework,
                                        repositoryUrl = it.repositoryUrl,
                                        branch = it.branch,
                                        rootDirectory = it.rootDirectory,
                                        buildCommand = it.buildCommand,
                                        installCommand = it.installCommand,
                                        startCommand = it.startCommand,
                                        envVariables = it.envVariables,
                                    ),
                                ).id
                        }
                    } ?: service.targetId

                ServiceType.DATABASE ->
                    request.databaseResource?.let {
                        val currentTargetId = service.targetId
                        if (currentTargetId != null) {
                            databaseResourceRepository.findByIdOrNull(currentTargetId)?.update(
                                type = it.type,
                                database = it.database,
                                username = it.username,
                                password = encryptionService.encrypt(it.password),
                            )
                            currentTargetId
                        } else {
                            databaseResourceRepository
                                .save(
                                    DatabaseResourceEntity(
                                        userId = userId,
                                        type = it.type,
                                        database = it.database,
                                        username = it.username,
                                        password = encryptionService.encrypt(it.password),
                                    ),
                                ).id
                        }
                    } ?: service.targetId

                ServiceType.DOMAIN ->
                    request.domainResource?.let {
                        val currentTargetId = service.targetId
                        if (currentTargetId != null) {
                            domainResourceRepository.findByIdOrNull(currentTargetId)?.update(
                                subdomain = it.subdomain,
                                customDomain = it.customDomain,
                            )
                            currentTargetId
                        } else {
                            domainResourceRepository
                                .save(
                                    DomainResourceEntity(
                                        userId = userId,
                                        subdomain = it.subdomain,
                                        customDomain = it.customDomain,
                                    ),
                                ).id
                        }
                    } ?: service.targetId

                else -> service.targetId
            }

        service.update(
            name = request.name,
            description = request.description,
            type = request.type,
            targetId = targetId,
        )
    }

    @Transactional
    fun updateServicePosition(
        serviceId: UUID,
        request: UpdateServicePositionRequest,
    ) {
        val userId = securityHolder.getUserId()
        val service = serviceRepository.findByIdAndDeletedAtIsNull(serviceId)
            ?: throw BusinessException(ServiceError.SERVICE_NOT_FOUND)

        if (service.userId != userId) {
            throw BusinessException(ServiceError.SERVICE_ACCESS_DENIED)
        }

        service.updatePosition(
            x = request.x,
            y = request.y,
        )
    }

    @Transactional
    fun deleteService(serviceId: UUID) {
        val userId = securityHolder.getUserId()
        val service = serviceRepository.findByIdAndDeletedAtIsNull(serviceId)
            ?: throw BusinessException(ServiceError.SERVICE_NOT_FOUND)

        if (service.userId != userId) {
            throw BusinessException(ServiceError.SERVICE_ACCESS_DENIED)
        }

        connectionRepository
            .findAllBySourceIdOrTargetIdAndDeletedAtIsNull(serviceId, serviceId)
            .forEach { it.delete() }

        val targetId = service.targetId
        if (targetId != null) {
            when (service.type) {
                ServiceType.APP -> appResourceRepository.deleteById(targetId)
                ServiceType.DATABASE -> databaseResourceRepository.deleteById(targetId)
                ServiceType.DOMAIN -> domainResourceRepository.deleteById(targetId)
                else -> {}
            }
        }

        service.delete()
    }

    private fun validateProjectAccess(
        projectId: UUID,
        userId: UUID,
    ) {
        val project = projectRepository.findByIdAndDeletedAtIsNull(projectId)
            ?: throw BusinessException(ProjectError.PROJECT_NOT_FOUND)

        if (project.userId != userId) {
            throw BusinessException(ProjectError.PROJECT_ACCESS_DENIED)
        }
    }
}
