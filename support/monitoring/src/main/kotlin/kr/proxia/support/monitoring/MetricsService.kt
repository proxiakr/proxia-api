package kr.proxia.support.monitoring

import io.micrometer.core.instrument.Counter
import io.micrometer.core.instrument.MeterRegistry
import io.micrometer.core.instrument.Timer
import org.springframework.stereotype.Service
import java.util.concurrent.TimeUnit

@Service
class MetricsService(
    private val registry: MeterRegistry,
) {
    fun incrementCounter(
        name: String,
        vararg tags: String,
    ) {
        Counter
            .builder(name)
            .tags(*tags)
            .register(registry)
            .increment()
    }

    fun recordTime(
        name: String,
        timeMs: Long,
        vararg tags: String,
    ) {
        Timer
            .builder(name)
            .tags(*tags)
            .register(registry)
            .record(timeMs, TimeUnit.MILLISECONDS)
    }

    fun <T> recordTime(
        name: String,
        vararg tags: String,
        block: () -> T,
    ): T {
        val timer =
            Timer
                .builder(name)
                .tags(*tags)
                .register(registry)
        return timer.recordCallable(block)!!
    }

    fun gauge(
        name: String,
        value: Number,
        vararg tags: String,
    ) {
        registry.gauge(
            name,
            io.micrometer.core.instrument.Tags
                .of(*tags),
            value,
        )
    }
}
