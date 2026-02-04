package kr.proxia.support.monitoring

import io.micrometer.core.instrument.Counter
import io.micrometer.core.instrument.MeterRegistry
import io.micrometer.core.instrument.Timer
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class MetricsConfig(
    private val registry: MeterRegistry,
) {
    @Bean
    fun httpRequestCounter(): Counter =
        Counter
            .builder("proxia.http.requests")
            .description("Total HTTP requests")
            .register(registry)

    @Bean
    fun httpRequestTimer(): Timer =
        Timer
            .builder("proxia.http.request.duration")
            .description("HTTP request duration")
            .register(registry)

    @Bean
    fun businessOperationCounter(): Counter =
        Counter
            .builder("proxia.business.operations")
            .description("Total business operations")
            .register(registry)
}
