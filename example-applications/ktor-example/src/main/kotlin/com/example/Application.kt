package com.example

import ExpirationMonitor
import io.ktor.server.application.*
import io.ktor.server.metrics.micrometer.MicrometerMetrics
import io.ktor.server.response.respond
import io.ktor.server.routing.get
import io.ktor.server.routing.routing
import io.micrometer.prometheus.PrometheusConfig
import io.micrometer.prometheus.PrometheusMeterRegistry
import java.io.File
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
    val expirationMonitor = ExpirationMonitor(StaticVariables.CLOCK, appMicrometerRegistry)

    expirationMonitor.monitorExpiringArtifact(StaticVariables.EXPIRING_CREDENTIAL)
    expirationMonitor.monitorExpiringArtifact(StaticVariables.X509)
    expirationMonitor.monitorExpiringArtifact(StaticVariables.PLAINTEXT_X509)
    expirationMonitor.monitorExpiringArtifacts(StaticVariables.P12.expiringCertificates)

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
    val X509 = ExpiringX509Certificate(
        name = "SomeX509",
        File(ClassLoader.getSystemResource("x509Certificate.crt").file)
    )
    val P12 = ExpiringPkcs12(
        name = "SomeP12",
        File(ClassLoader.getSystemResource("keystore.pfx").file),
        ""
    )
    val PLAINTEXT_X509 = ExpiringX509Certificate(
        name = "SomePlaintextX509",
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
