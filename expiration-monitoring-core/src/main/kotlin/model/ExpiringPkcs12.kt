package model

import exception.ArtifactParsingException
import io.micrometer.core.instrument.ImmutableTag
import java.io.File
import java.io.FileInputStream
import java.security.KeyStore
import java.security.cert.X509Certificate

class ExpiringPkcs12(
    name: String,
    url: File,
    password: String,
    tags: Collection<ImmutableTag> = emptyList()
) {

    val expiringCertificates: Collection<ExpiringX509Certificate> = extractCertificates(name, tags, url, password)

    private fun extractCertificates(
        name: String,
        tags: Collection<ImmutableTag>,
        url: File,
        password: String
    ): Collection<ExpiringX509Certificate> =
        runCatching {
            val keyStore = KeyStore.getInstance("pkcs12")
            FileInputStream(url).use {
                keyStore.load(it, password.toCharArray())
            }

            return keyStore.aliases().toList().map {
                ExpiringX509Certificate("$name-$it", keyStore.getCertificate(it) as X509Certificate, tags)
            }
        }.getOrElse {
            throw ArtifactParsingException("Exception during extracting certificates from PKCS12 store $name.", it)
        }
}
