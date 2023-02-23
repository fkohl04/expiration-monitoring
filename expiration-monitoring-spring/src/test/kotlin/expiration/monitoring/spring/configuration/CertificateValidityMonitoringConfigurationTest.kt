package expiration.monitoring.spring.configuration

import io.micrometer.core.instrument.ImmutableTag
import io.micrometer.core.instrument.Measurement
import io.micrometer.core.instrument.Meter
import io.micrometer.core.instrument.MeterRegistry
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles
import strikt.api.expectThat
import strikt.assertions.containsExactlyInAnyOrder
import strikt.assertions.isA
import strikt.assertions.isGreaterThan
import strikt.assertions.isNotNull

@SpringBootTest
@ActiveProfiles("monitoring-test")
class CertificateValidityMonitoringConfigurationTest {

    @Autowired
    lateinit var registry: MeterRegistry

    val globalTag = ImmutableTag("service", "test-service")
    val artifactExpirationMetricName = "artifact.name"

    @Nested
    inner class ExpiringCredentialTests {

        @Test
        fun `When expiring credential is provided via application config, then it is parsed and monitored with expected tags`() {
            val artifactNameTag = ImmutableTag(artifactExpirationMetricName, "test credential")

            val artifactExpirationMeter = registry.meters.find { it.id.tags.contains(artifactNameTag) }

            expectThatMeterExistsAndHasProperTags(artifactExpirationMeter, artifactNameTag)
        }

    }

    @Nested
    inner class ExpiringX509CertificateTests {
        @Test
        fun `When expiring x509Certificate is provided via application config, then it is parsed and monitored with expected tags`() {
            val artifactNameTag = ImmutableTag(artifactExpirationMetricName, "test X509 certificate")

            val artifactExpirationMeter = registry.meters.find { it.id.tags.contains(artifactNameTag) }

            expectThatMeterExistsAndHasProperTags(artifactExpirationMeter, artifactNameTag)
        }

        @Test
        fun `When an non parsable expiring x509Certificate is provided via application config, then the application starts and a substitute of the credential is monitored with expected tags`() {
            val artifactNameTag =
                ImmutableTag(artifactExpirationMetricName, "Parsing Error: Not parsable X509 certificate")

            val artifactExpirationMeter = registry.meters.find { it.id.tags.contains(artifactNameTag) }

            expectThatMeterExistsAndHasProperTags(artifactExpirationMeter, artifactNameTag)
        }
    }

    @Nested
    inner class ExpiringxPkcs12StoreTests {
        @Test
        fun `When expiring pkcs12Store is provided via application config, then it is parsed and monitored with expected tags`() {
            val artifactNameTag = ImmutableTag(artifactExpirationMetricName, "test p12 keystore-1")

            val artifactExpirationMeter = registry.meters.find { it.id.tags.contains(artifactNameTag) }

            expectThatMeterExistsAndHasProperTags(artifactExpirationMeter, artifactNameTag)
        }

        @Test
        fun `When an non parsable expiring x509Certificate is provided via application config, then the application starts and a substitute of the credential is monitored with expected tags`() {
            val artifactNameTag = ImmutableTag(artifactExpirationMetricName, "Parsing Error: Not parsable p12 keystore")

            val artifactExpirationMeter = registry.meters.find { it.id.tags.contains(artifactNameTag) }

            expectThatMeterExistsAndHasProperTags(artifactExpirationMeter, artifactNameTag)
        }
    }

    @Nested
    inner class ExpiringPlaintextX509CertificateTests {
        @Test
        fun `When expiring plaintext x509Certificate is provided via application config, then it is parsed and monitored with expected tags`() {
            val artifactNameTag = ImmutableTag(artifactExpirationMetricName, "test plaintext X509 certificate")

            val artifactExpirationMeter = registry.meters.find { it.id.tags.contains(artifactNameTag) }

            expectThatMeterExistsAndHasProperTags(artifactExpirationMeter, artifactNameTag)
        }

        @Test
        fun `When an non parsable expiring x509Certificate is provided via application config, then the application starts and a substitute of the credential is monitored with expected tags`() {
            val artifactNameTag =
                ImmutableTag(artifactExpirationMetricName, "Parsing Error: Not parsable plaintext X509 certificate")

            val artifactExpirationMeter = registry.meters.find { it.id.tags.contains(artifactNameTag) }

            expectThatMeterExistsAndHasProperTags(artifactExpirationMeter, artifactNameTag)
        }
    }

    private fun expectThatMeterExistsAndHasProperTags(
        artifactExpirationMeter: Meter?,
        artifactNameTag: ImmutableTag
    ) {
        expectThat(artifactExpirationMeter).isNotNull().and {
            get { id.tags }.containsExactlyInAnyOrder(globalTag, artifactNameTag)
            get { measure() }.isA<Collection<Measurement>>().and {
                get { size }.isGreaterThan(0)
            }
        }
    }
}
