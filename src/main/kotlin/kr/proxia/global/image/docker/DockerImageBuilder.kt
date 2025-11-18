package kr.proxia.global.image.docker

import com.github.dockerjava.api.command.BuildImageResultCallback
import io.github.oshai.kotlinlogging.KotlinLogging
import kr.proxia.global.container.docker.factory.DockerClientFactory
import kr.proxia.global.image.ImageBuilder
import org.springframework.stereotype.Component
import java.io.File

private val logger = KotlinLogging.logger {}

@Component
class DockerImageBuilder(
    private val dockerClientFactory: DockerClientFactory,
) : ImageBuilder {
    override fun buildImage(
        endpoint: String,
        contextDir: File,
        dockerfile: File,
        imageName: String,
        tags: Set<String>,
    ): String {
        try {
            val client = dockerClientFactory.getClient(endpoint)

            val fullTags = tags.map { "$imageName:$it" }.toSet()

            val imageId =
                client
                    .buildImageCmd()
                    .withDockerfile(dockerfile)
                    .withBaseDirectory(contextDir)
                    .withTags(fullTags)
                    .exec(BuildImageResultCallback())
                    .awaitImageId()

            logger.info { "Built Docker image: $imageName with ID: $imageId" }
            return imageId
        } catch (e: Exception) {
            logger.error(e) { "Failed to build image: $imageName" }
            throw e
        }
    }
}
