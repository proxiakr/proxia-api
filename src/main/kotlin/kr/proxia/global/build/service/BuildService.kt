package kr.proxia.global.build.service

import io.fabric8.kubernetes.api.model.batch.v1.Job
import io.fabric8.kubernetes.api.model.batch.v1.JobBuilder
import io.fabric8.kubernetes.client.KubernetesClient
import kr.proxia.global.build.properties.BuildProperties
import kr.proxia.global.git.service.GitService
import kr.proxia.global.kubernetes.properties.KubernetesProperties
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.stereotype.Service
import java.time.Duration

data class BuildResult(
    val imageTag: String,
    val commitSha: String,
    val cached: Boolean,
)

@Service
@EnableConfigurationProperties(BuildProperties::class)
class BuildService(
    private val kubernetesClient: KubernetesClient,
    private val kubernetesProperties: KubernetesProperties,
    private val buildProperties: BuildProperties,
    private val gitService: GitService,
) {
    fun build(
        gitUrl: String,
        branch: String,
        imageName: String,
    ): BuildResult {
        val commitSha = gitService.getCommitSha(gitUrl, branch)
        val imageTag = "${buildProperties.registry}/$imageName:$commitSha"

        if (imageExists(imageTag)) {
            return BuildResult(
                imageTag = imageTag,
                commitSha = commitSha,
                cached = true,
            )
        }

        val job = createBuildJob(gitUrl, branch, imageTag)

        kubernetesClient
            .batch()
            .v1()
            .jobs()
            .inNamespace(kubernetesProperties.namespace)
            .resource(job)
            .create()

        waitForCompletion(job.metadata.name)

        return BuildResult(
            imageTag = imageTag,
            commitSha = commitSha,
            cached = false,
        )
    }

    private fun imageExists(imageTag: String): Boolean {
        val (registry, imagePath) = imageTag.split("/", limit = 2)
        val (name, tag) = imagePath.split(":", limit = 2)

        return runCatching {
            val url = java.net.URL("http://$registry/v2/$name/manifests/$tag")
            val connection = url.openConnection() as java.net.HttpURLConnection
            connection.requestMethod = "HEAD"
            connection.responseCode == 200
        }.getOrDefault(false)
    }

    private fun createBuildJob(
        gitUrl: String,
        branch: String,
        destination: String,
    ): Job =
        JobBuilder()
            .withNewMetadata()
            .withGenerateName("build-")
            .endMetadata()
            .withNewSpec()
            .withBackoffLimit(0)
            .withTtlSecondsAfterFinished(300)
            .withNewTemplate()
            .withNewSpec()
            .addNewContainer()
            .withName("kaniko")
            .withImage("gcr.io/kaniko-project/executor:v1.23.0")
            .withArgs(
                "--dockerfile=Dockerfile",
                "--context=git://$gitUrl#refs/heads/$branch",
                "--destination=$destination",
                "--insecure",
                "--cache=true",
                "--cache-repo=$destination",
                "--compressed-caching=false",
                "--single-snapshot",
                "--cleanup",
            ).withNewResources()
            .withRequests<String, io.fabric8.kubernetes.api.model.Quantity>(
                mapOf(
                    "memory" to
                        io.fabric8.kubernetes.api.model
                            .Quantity("512Mi"),
                    "cpu" to
                        io.fabric8.kubernetes.api.model
                            .Quantity("500m"),
                ),
            ).withLimits<String, io.fabric8.kubernetes.api.model.Quantity>(
                mapOf(
                    "memory" to
                        io.fabric8.kubernetes.api.model
                            .Quantity("2Gi"),
                    "cpu" to
                        io.fabric8.kubernetes.api.model
                            .Quantity("2"),
                ),
            ).endResources()
            .endContainer()
            .withRestartPolicy("Never")
            .endSpec()
            .endTemplate()
            .endSpec()
            .build()

    private fun waitForCompletion(
        jobName: String,
        timeout: Duration = Duration.ofMinutes(buildProperties.timeoutMinutes),
    ) {
        val endTime = System.currentTimeMillis() + timeout.toMillis()
        var lastLogLine = ""

        while (System.currentTimeMillis() < endTime) {
            val job =
                kubernetesClient
                    .batch()
                    .v1()
                    .jobs()
                    .inNamespace(kubernetesProperties.namespace)
                    .withName(jobName)
                    .get() ?: throw BuildException("Job $jobName not found")

            val status = job.status

            when {
                status?.succeeded == 1 -> {
                    cleanupJob(jobName)
                    return
                }
                status?.failed ?: 0 > 0 -> {
                    val logs = getJobLogs(jobName)
                    cleanupJob(jobName)
                    throw BuildException("Build failed:\n${logs.takeLast(500)}")
                }
                else -> {
                    val currentLog = getJobLogs(jobName).lines().lastOrNull() ?: ""
                    if (currentLog != lastLogLine && currentLog.isNotBlank()) {
                        println("[Build] $currentLog")
                        lastLogLine = currentLog
                    }
                }
            }

            Thread.sleep(2000)
        }

        cleanupJob(jobName)
        throw BuildException("Build timeout after ${timeout.toMinutes()} minutes")
    }

    private fun getJobLogs(jobName: String): String =
        kubernetesClient
            .pods()
            .inNamespace(kubernetesProperties.namespace)
            .withLabel("job-name", jobName)
            .list()
            .items
            .firstOrNull()
            ?.let { pod ->
                kubernetesClient
                    .pods()
                    .inNamespace(kubernetesProperties.namespace)
                    .withName(pod.metadata.name)
                    .log
            } ?: ""

    private fun cleanupJob(jobName: String) {
        kubernetesClient
            .batch()
            .v1()
            .jobs()
            .inNamespace(kubernetesProperties.namespace)
            .withName(jobName)
            .delete()

        kubernetesClient
            .pods()
            .inNamespace(kubernetesProperties.namespace)
            .withLabel("job-name", jobName)
            .delete()
    }
}

class BuildException(
    message: String,
) : RuntimeException(message)
