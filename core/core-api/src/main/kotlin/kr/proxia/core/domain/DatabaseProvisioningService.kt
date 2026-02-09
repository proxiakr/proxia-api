package kr.proxia.core.domain

import kr.proxia.client.docker.DockerClient
import kr.proxia.client.docker.VolumeBinding
import kr.proxia.core.enums.ServiceStatus
import kr.proxia.core.support.error.CoreException
import kr.proxia.core.support.error.ErrorType
import kr.proxia.storage.db.core.entity.DatabaseService
import kr.proxia.storage.db.core.repository.DatabaseServiceRepository
import kr.proxia.support.logging.logger
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class DatabaseProvisioningService(
    private val dockerClient: DockerClient,
    private val dockerNetworkService: DockerNetworkService,
    private val databaseServiceRepository: DatabaseServiceRepository,
) {
    private val log = logger()

    @Transactional
    fun provision(databaseService: DatabaseService) {
        val config = DatabaseEngineConfig.of(databaseService.engine)
        if (databaseService.version !in config.supportedVersions) {
            throw CoreException(ErrorType.UNSUPPORTED_DATABASE_VERSION)
        }
        val image = "${config.image}:${databaseService.version}"
        val containerName = DockerNaming.container(databaseService.id)
        val volumeName = DockerNaming.volume(databaseService.id)
        val projectId = databaseService.project.id
        val networkName = DockerNaming.network(projectId)

        updateStatus(databaseService, ServiceStatus.STARTING)

        try {
            dockerNetworkService.ensureNetworkExists(projectId)

            pullImage(config.image, databaseService.version)

            dockerClient.createVolume(volumeName)

            val env =
                config.env(databaseService.database, databaseService.username, databaseService.password)

            val containerId =
                createContainer(
                    image = image,
                    name = containerName,
                    env = env,
                    networkName = networkName,
                    volumeBinding = VolumeBinding(volumeName, config.dataPath),
                )
            startContainer(containerId)

            updateStatus(databaseService, ServiceStatus.RUNNING)
            log.info { "Provisioned database service: ${databaseService.id} ($containerName)" }
        } catch (e: CoreException) {
            updateStatus(databaseService, ServiceStatus.FAILED)
            throw e
        } catch (e: Exception) {
            updateStatus(databaseService, ServiceStatus.FAILED)
            log.error(e) { "Failed to provision database service: ${databaseService.id}" }
            throw CoreException(ErrorType.DOCKER_CONTAINER_CREATE_FAILED)
        }
    }

    @Transactional
    fun deprovision(databaseService: DatabaseService) {
        val containerName = DockerNaming.container(databaseService.id)

        updateStatus(databaseService, ServiceStatus.STOPPING)

        try {
            dockerClient.stopContainer(containerName)
            dockerClient.removeContainer(containerName)

            updateStatus(databaseService, ServiceStatus.STOPPED)
            log.info { "Deprovisioned database service: ${databaseService.id} ($containerName)" }
        } catch (e: Exception) {
            updateStatus(databaseService, ServiceStatus.FAILED)
            log.error(e) { "Failed to deprovision database service: ${databaseService.id}" }
            throw CoreException(ErrorType.DOCKER_CONTAINER_STOP_FAILED)
        }
    }

    private fun updateStatus(
        databaseService: DatabaseService,
        status: ServiceStatus,
    ) {
        databaseService.status = status
        databaseServiceRepository.save(databaseService)
    }

    private fun pullImage(
        image: String,
        tag: String,
    ) {
        try {
            dockerClient.pullImage(image, tag)
        } catch (e: Exception) {
            log.error(e) { "Failed to pull image: $image:$tag" }
            throw CoreException(ErrorType.DOCKER_IMAGE_PULL_FAILED)
        }
    }

    private fun createContainer(
        image: String,
        name: String,
        env: List<String>,
        networkName: String,
        volumeBinding: VolumeBinding,
    ): String {
        try {
            return dockerClient.createContainer(image, name, env, networkName, volumeBinding)
        } catch (e: Exception) {
            log.error(e) { "Failed to create container: $name" }
            throw CoreException(ErrorType.DOCKER_CONTAINER_CREATE_FAILED)
        }
    }

    private fun startContainer(containerId: String) {
        try {
            dockerClient.startContainer(containerId)
        } catch (e: Exception) {
            log.error(e) { "Failed to start container: $containerId" }
            throw CoreException(ErrorType.DOCKER_CONTAINER_START_FAILED)
        }
    }
}
