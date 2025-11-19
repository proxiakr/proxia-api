package kr.proxia

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.context.properties.ConfigurationPropertiesScan
import org.springframework.boot.runApplication
import org.springframework.scheduling.annotation.EnableAsync

@EnableAsync
@SpringBootApplication
@ConfigurationPropertiesScan
class ProxiaApplication

fun main(args: Array<String>) {
    runApplication<ProxiaApplication>(*args)
}
