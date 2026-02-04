package kr.proxia.core.api.controller

import kr.proxia.support.logging.logger
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RestController

@RestController
class HealthController {
    private val log = logger()

    @GetMapping("/health")
    fun health(): Map<String, String> {
        log.info { "Health check requested" }
        return mapOf("status" to "UP")
    }
}
