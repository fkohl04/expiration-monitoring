package expiration.monitoring.spring.configuration

import ExpirationMonitor
import expiration.monitoring.spring.configuration.properties.ExpirationMonitoringProperties
import expiration.monitoring.spring.configuration.properties.ExpiringCredentialProperties
import expiration.monitoring.spring.configuration.properties.ExpiringPkcs12Properties
import expiration.monitoring.spring.configuration.properties.ExpiringPlaintextX509CertificateProperties
import expiration.monitoring.spring.configuration.properties.ExpiringX509CertificateProperties
import io.micrometer.core.instrument.MeterRegistry
import java.time.Clock
import model.ExpiringCredential
import model.ExpiringPkcs12
import model.ExpiringX509Certificate
import org.slf4j.LoggerFactory
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Suppress("SpringJavaInjectionPointsAutowiringInspection")
@Configuration
open class CertificateValidityMonitoringConfiguration {

    companion object {
        private val logger = LoggerFactory.getLogger(CertificateValidityMonitoringConfiguration::class.java)
    }

    /**
     * Creates a [ExpirationMonitor] that monitors the validity of all certificates, raw certificates and p12
     * key stores that are specified by the [ExpirationMonitoringProperties].
     * If one of the input values is not readable or parsable the certificate monitor will expose a metric that marks
     * the certificate as expired.
     */
    @Bean
    open fun expirationMonitor(
        clock: Clock,
        registry: MeterRegistry?,
        expirationMonitoringProperties: ExpirationMonitoringProperties?
    ): ExpirationMonitor? {
        if (registry == null || expirationMonitoringProperties == null) {
            logger.warn("Not initializing certificate monitoring due to missing dependencies")
            return null
        }

        val certificateValidityMonitor = ExpirationMonitor(clock, registry)

        certificateValidityMonitor.monitorCredentials(expirationMonitoringProperties.credentials)
        certificateValidityMonitor.monitorX509Certificates(expirationMonitoringProperties.x509Certificates)
        certificateValidityMonitor.monitorPlaintextX509Certificates(expirationMonitoringProperties.plaintextX509Certificates)
        certificateValidityMonitor.monitorPKCS12Stores(expirationMonitoringProperties.pkcs12Stores)

        return certificateValidityMonitor
    }

    private fun ExpirationMonitor.monitorCredentials(
        credentials: List<ExpiringCredentialProperties>?
    ) {
        credentials?.forEach { properties ->
            receiveArtifactSafelyAndMonitor(properties.name) {
                ExpiringCredential(properties.name, properties.expirationDate)
            }
        }
    }

    private fun ExpirationMonitor.monitorX509Certificates(
        certificates: List<ExpiringX509CertificateProperties>?
    ) {
        certificates?.forEach { properties ->
            receiveArtifactSafelyAndMonitor(properties.name) {
                ExpiringX509Certificate(properties.name, properties.location)
            }
        }
    }

    private fun ExpirationMonitor.monitorPlaintextX509Certificates(
        plaintextX509Certificates: List<ExpiringPlaintextX509CertificateProperties>?
    ) {
        plaintextX509Certificates?.forEach { properties ->
            receiveArtifactSafelyAndMonitor(properties.name) {
                ExpiringX509Certificate(properties.name, properties.content)
            }
        }
    }

    private fun ExpirationMonitor.monitorPKCS12Stores(stores: List<ExpiringPkcs12Properties>?) {
        stores?.forEach { properties ->
            receiveArtifactsSafelyAndMonitor(properties.name) {
                ExpiringPkcs12(properties.name, properties.location, properties.password)
                    .expiringCertificates
            }
        }
    }
}
