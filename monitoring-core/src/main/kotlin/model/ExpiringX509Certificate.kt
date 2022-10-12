package model

import io.micrometer.core.instrument.ImmutableTag
import java.io.File
import java.io.FileInputStream
import java.io.InputStream
import java.security.cert.CertificateFactory
import java.security.cert.X509Certificate
import java.util.Date
import org.slf4j.Logger
import org.slf4j.LoggerFactory

class ExpiringX509Certificate(
    override val name: String,
    override val expirationDate: Date?,
    override val tags: Collection<ImmutableTag>,
): ExpiringArtifact {

    companion object {
        val logger: Logger = LoggerFactory.getLogger(ExpiringX509Certificate::class.java)
    }

    constructor(name: String, url: File, tags: Collection<ImmutableTag> = emptyList()) : this(
        name,
        FileInputStream(url).extractExpiryDate(name),
        tags
    )

    constructor(name: String, content: String, tags: Collection< ImmutableTag> = emptyList()) : this(
        name,
        content.byteInputStream().extractExpiryDate(name),
        tags
    )

    constructor(name: String, certificate: X509Certificate, tags: Collection< ImmutableTag> = emptyList()) : this(
        name,
        certificate.notAfter,
        tags
    )
}

private fun InputStream.extractExpiryDate(name: String) = runCatching {
    this.use { stream ->
        return@use CertificateFactory.getInstance("X509").generateCertificate(stream) as X509Certificate
    }.notAfter
}.getOrElse {
    ExpiringX509Certificate.logger.warn("Exception during monitoring of expiration of X509 certificate $name.", it)
    null
}
