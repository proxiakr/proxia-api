package kr.proxia.global.build.properties

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties(prefix = "build")
data class BuildProperties(
    val registry: String = "localhost:5000",
    val timeoutMinutes: Long = 10,
)
