package com.example

import ExpirationMonitor
import io.ktor.server.application.*
import io.ktor.server.metrics.micrometer.MicrometerMetrics
import io.ktor.server.response.respond
import io.ktor.server.routing.get
import io.ktor.server.routing.routing
import io.micrometer.prometheus.PrometheusConfig
import io.micrometer.prometheus.PrometheusMeterRegistry
import java.time.Clock
import java.time.Instant
import java.time.temporal.ChronoUnit
import java.util.Date
import model.ExpiringCredential

fun main(args: Array<String>): Unit =
    io.ktor.server.netty.EngineMain.main(args)

@Suppress("unused") // application.conf references the main function. This annotation prevents the IDE from marking it as unused.
fun Application.module() {
    val appMicrometerRegistry = PrometheusMeterRegistry(PrometheusConfig.DEFAULT)
    val expirationMonitor = ExpirationMonitor(StaticVariables.CLOCK, appMicrometerRegistry)
    expirationMonitor.monitorExpiringArtifact(StaticVariables.EXPIRING_CREDENTIAL)

    install(MicrometerMetrics) {
        registry = appMicrometerRegistry
    }

    routing {
        get("/metrics-micrometer") {
            call.respond(appMicrometerRegistry.scrape())
        }
    }
}

object StaticVariables {
    val CLOCK: Clock = Clock.systemUTC()
    val EXPIRING_CREDENTIAL = ExpiringCredential(
        "SomeExpiringCredential",
        Date.from(Instant.now(CLOCK).plus(5, ChronoUnit.DAYS))
    )
}
