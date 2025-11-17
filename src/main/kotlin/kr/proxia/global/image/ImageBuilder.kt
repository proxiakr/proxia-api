package kr.proxia.global.image

import java.io.File

interface ImageBuilder {
    fun buildImage(
        endpoint: String,
        contextDir: File,
        dockerfile: File,
        imageName: String,
        tags: Set<String> = setOf("latest"),
    ): String
}
