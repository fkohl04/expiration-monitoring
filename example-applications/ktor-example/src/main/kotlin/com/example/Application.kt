package com.example

import ExpirationMonitor
import io.ktor.server.application.Application
import io.ktor.server.application.ApplicationEnvironment
import io.ktor.server.application.call
import io.ktor.server.application.install
import io.ktor.server.metrics.micrometer.MicrometerMetrics
import io.ktor.server.response.respond
import io.ktor.server.routing.get
import io.ktor.server.routing.routing
import io.micrometer.core.instrument.ImmutableTag
import io.micrometer.prometheus.PrometheusConfig
import io.micrometer.prometheus.PrometheusMeterRegistry
import java.io.File
import java.io.FileNotFoundException
import java.time.Clock
import java.time.Instant
import java.time.temporal.ChronoUnit
import java.util.Date
import model.ExpiringCredential
import model.ExpiringPkcs12
import model.ExpiringX509Certificate

fun main(args: Array<String>): Unit =
    io.ktor.server.netty.EngineMain.main(args)

@Suppress("unused") // application.conf references the main function. This annotation prevents the IDE from marking it as unused.
fun Application.module() {
    val appMicrometerRegistry = PrometheusMeterRegistry(PrometheusConfig.DEFAULT)

    install(MicrometerMetrics) {
        registry = appMicrometerRegistry
    }

    routing {
        get("/metrics-micrometer") {
            call.respond(appMicrometerRegistry.scrape())
        }
    }

    createAndMonitorExpiringArtifacts(appMicrometerRegistry, environment)
}

private fun createAndMonitorExpiringArtifacts(
    prometheusMeterRegistry: PrometheusMeterRegistry,
    environment: ApplicationEnvironment
) {
    val clock: Clock = Clock.systemUTC()
    val expirationMonitor = ExpirationMonitor(
        clock,
        prometheusMeterRegistry,
        listOf(ImmutableTag("service", "ktor-example"))
    )

    "SomeExpiringCredential".let {
        expirationMonitor.receiveArtifactSafelyAndMonitor(it) {
            ExpiringCredential(it, Date.from(Instant.now(clock).plus(5, ChronoUnit.DAYS)))
        }
    }
    "AnotherExpiringCredential".let {
        expirationMonitor.receiveArtifactSafelyAndMonitor(it) {
            ExpiringCredential(it, Date.from(Instant.now(clock).plus(35, ChronoUnit.DAYS)))
        }
    }
    "SomeX509".let {
        expirationMonitor.receiveArtifactSafelyAndMonitor(it) {
            ExpiringX509Certificate(it, File(environment.config.property("expiration.monitoring.x509.location").getString()))
        }
    }
    "SomeP12".let {
        expirationMonitor.receiveArtifactsSafelyAndMonitor(it) {
            ExpiringPkcs12(
                it, File(environment.config.property("expiration.monitoring.pkcs12.location").getString()), ""
            ).expiringCertificates
        }
    }
    "SomePlaintextX509".let {
        expirationMonitor.receiveArtifactSafelyAndMonitor(it) {
            ExpiringX509Certificate(
                name = it,
                content = """
            -----BEGIN CERTIFICATE-----
            MIICljCCAX4CCQDERzAAFiDvTzANBgkqhkiG9w0BAQsFADANMQswCQYDVQQGEwJE
            RTAeFw0yMjEyMTUxMzQyNDVaFw0yMzEyMTUxMzQyNDVaMA0xCzAJBgNVBAYTAkRF
            MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAxWiHLT8USaHdixSoZEU7
            pOZdOU96a1GAkCbgVC600T/KTqJC6TIPqe9VejFHBlmBuF4BMVczmOHPD0K7lWdU
            ZzJOGAxgtplNVgUZLLIv6KSMYuMg6yWY0aAHmwKJm3OfuEmygco3zgcKGqnzsttz
            Ttw2cyO5asgNQfP0J/xPDVxEjnLLoj67HrqH/4wDT7dLgG7ar8BeshcUIRVDld8q
            s8mTcTR8Ax5w8R7FTUBuJvuomtC4DHseUMRbLCIVAe1gK7kirDWWBtXAvY3wefqA
            qwYuw6ufyy1U5KL+5OiHY0Ddrk2MU3jSSM0X/AFJIKb34UpTyDkC02w0y9UDSd6D
            +wIDAQABMA0GCSqGSIb3DQEBCwUAA4IBAQABDGAeCnLosYBcBsO8bDLum437xbgK
            xYmpBxGI59lJt6wz6bXM4pJ45OJM1MEgQmrUyRBRJMLHro7fkWr3e6y2cRVvlZIq
            TpbrPp4hJMFz2nHOCXctqcCVTJlyprr6mrNpf/27DdlO349CDa1asumFCjuoZTCT
            S9b2PfdZDjvdTiMaFwJaAe5rtDcdU79gLTG5i5MzuqvXP063lPoeY/HCB3V5vrd9
            8pnO+DFKboH4L9yfsrpX5ZweC06hvCm3qOGp2wW7mXrTfSeCUOCJRUIDLNBAp0+2
            pSlM6plWfqPtMOhtn0e9/heckN6LjrHUSpEKIrEPvzcKrk4X1j6zD8lX
            -----END CERTIFICATE-----
            """.trimIndent()
            )
        }
    }
    "SomeCredentialThatCanNotBeParsed".let {
        expirationMonitor.receiveArtifactSafelyAndMonitor(it) {
            throw FileNotFoundException("This exception was thrown for testing purpose.")
        }
    }
}
