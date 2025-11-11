package kr.proxia.domain.webhook.application.service

import com.fasterxml.jackson.databind.ObjectMapper
import kr.proxia.domain.deployment.application.service.DeploymentService
import kr.proxia.domain.resource.domain.repository.AppResourceRepository
import kr.proxia.domain.service.domain.enums.ServiceType
import kr.proxia.domain.service.domain.repository.ServiceRepository
import kr.proxia.domain.webhook.domain.entity.WebhookEventEntity
import kr.proxia.domain.webhook.domain.repository.WebhookEventRepository
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class WebhookService(
    private val webhookEventRepository: WebhookEventRepository,
    private val serviceRepository: ServiceRepository,
    private val appResourceRepository: AppResourceRepository,
    private val deploymentService: DeploymentService,
    private val objectMapper: ObjectMapper,
) {
    private val logger = LoggerFactory.getLogger(javaClass)

    @Transactional
    fun handleGithubPushEvent(
        event: String,
        payload: Map<String, Any>,
        deliveryId: String?,
    ) {
        val repositoryUrl = extractRepositoryUrl(payload)
        val branch = extractBranch(payload)

        if (repositoryUrl == null) {
            logger.warn("No repository URL found in webhook payload")
            return
        }

        val matchingServices = findMatchingServices(repositoryUrl, branch)

        if (matchingServices.isEmpty()) {
            logger.info("No services found for repository: $repositoryUrl, branch: $branch")
            webhookEventRepository.save(
                WebhookEventEntity(
                    serviceId = null,
                    event = event,
                    payload = objectMapper.writeValueAsString(payload),
                    deliveryId = deliveryId,
                    success = true,
                    errorMessage = "No matching services found",
                ),
            )
            return
        }

        matchingServices.forEach { service ->
            try {
                logger.info("Triggering deployment for service ${service.id} from webhook")
                webhookEventRepository.save(
                    WebhookEventEntity(
                        serviceId = service.id,
                        event = event,
                        payload = objectMapper.writeValueAsString(payload),
                        deliveryId = deliveryId,
                        success = true,
                    ),
                )

                deploymentService.deploy(service.id!!, branch)
            } catch (e: Exception) {
                logger.error("Failed to trigger deployment for service ${service.id}", e)
                val webhookEvent =
                    webhookEventRepository.save(
                        WebhookEventEntity(
                            serviceId = service.id,
                            event = event,
                            payload = objectMapper.writeValueAsString(payload),
                            deliveryId = deliveryId,
                            success = false,
                            errorMessage = e.message,
                        ),
                    )
                webhookEvent.markAsFailed(e.message ?: "Unknown error")
                webhookEventRepository.save(webhookEvent)
            }
        }
    }

    private fun extractRepositoryUrl(payload: Map<String, Any>): String? {
        val repository = payload["repository"] as? Map<*, *> ?: return null
        return repository["clone_url"] as? String
            ?: repository["git_url"] as? String
            ?: repository["ssh_url"] as? String
    }

    private fun extractBranch(payload: Map<String, Any>): String? {
        val ref = payload["ref"] as? String ?: return null
        return ref.removePrefix("refs/heads/")
    }

    private fun findMatchingServices(
        repositoryUrl: String,
        branch: String?,
    ): List<kr.proxia.domain.service.domain.entity.ServiceEntity> {
        val allServices = serviceRepository.findAll()

        return allServices.filter { service ->
            if (service.type != ServiceType.APP || service.targetId == null) {
                return@filter false
            }

            val appResource = appResourceRepository.findById(service.targetId!!).orElse(null) ?: return@filter false

            val repoMatches = normalizeRepositoryUrl(appResource.repositoryUrl) == normalizeRepositoryUrl(repositoryUrl)
            val branchMatches = branch == null || appResource.branch == null || appResource.branch == branch

            repoMatches && branchMatches
        }
    }

    private fun normalizeRepositoryUrl(url: String?): String? {
        if (url == null) return null

        return url
            .removePrefix("https://")
            .removePrefix("http://")
            .removePrefix("git@")
            .replace(":", "/")
            .removeSuffix(".git")
            .lowercase()
    }
}
