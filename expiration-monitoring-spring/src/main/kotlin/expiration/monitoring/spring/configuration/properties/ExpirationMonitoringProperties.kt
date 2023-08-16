package expiration.monitoring.spring.configuration.properties

import io.micrometer.core.instrument.ImmutableTag
import org.springframework.boot.context.properties.ConfigurationProperties
import java.io.File
import java.util.Date

@ConfigurationProperties(value = "expiration.monitoring")
data class ExpirationMonitoringProperties(
    var globalTags: List<ImmutableTag>?,
    var credentials: List<ExpiringCredentialProperties>?,
    var pkcs12Stores: List<ExpiringPkcs12Properties>?,
    var x509Certificates: List<ExpiringX509CertificateProperties>?,
    var plaintextX509Certificates: List<ExpiringPlaintextX509CertificateProperties>?,
)

data class ExpiringPkcs12Properties(
    val name: String,
    val location: File,
    val password: String = ""
)

data class ExpiringX509CertificateProperties(
    val name: String,
    val location: File
)

data class ExpiringPlaintextX509CertificateProperties(
    val name: String,
    val content: String
)

data class ExpiringCredentialProperties(
    val name: String,
    val expirationDate: Date
)
