package kr.proxia.domain.monitoring.presentation.controller

import kr.proxia.domain.monitoring.application.service.LogStreamingService
import org.springframework.http.MediaType
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter

@RestController
@RequestMapping("/api/services")
class LogStreamingController(
    private val logStreamingService: LogStreamingService,
) {
    @GetMapping("/{serviceId}/logs", produces = [MediaType.TEXT_EVENT_STREAM_VALUE])
    fun streamLogs(
        @PathVariable serviceId: Long,
        @RequestParam(defaultValue = "100") tail: Int,
        @RequestParam(defaultValue = "true") follow: Boolean,
    ): SseEmitter = logStreamingService.streamLogs(serviceId, tail, follow)
}
