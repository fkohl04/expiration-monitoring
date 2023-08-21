import exception.ArtifactParsingException
import java.io.File
import java.io.FileOutputStream
import java.nio.file.Path
import java.security.KeyStore
import java.security.cert.X509Certificate
import java.time.Instant
import model.ExpiringPkcs12
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ValueSource
import strikt.api.expectThat
import strikt.api.expectThrows
import strikt.assertions.hasSize
import strikt.assertions.isEqualTo
import utils.X509CertificateFactory

internal class ExpiringPkcs12Test {

    private val password = "some password"
    private val keyStoreName = "some keystore name"

    @ParameterizedTest
    @ValueSource(ints = [0, 1, 5])
    fun `Given keystore with certain count of certificates, when keystore is created from file, Then all contained certificates are parsed`(
        certificateCount: Int,
        @TempDir tempDir: Path
    ) {
        val pathToKeystore = createKeyStoreFileContainingCertificates(
            (1..certificateCount).associate { "$it" to X509CertificateFactory.generateX509Certificate() },
            tempDir
        )

        val uut = ExpiringPkcs12(keyStoreName, pathToKeystore, password)

        expectThat(uut.expiringCertificates) hasSize certificateCount
    }

    @Test
    fun `Given keystore with 1 certificate, when keystore is created from file, Then name of certificate and expiry date is correct`(
        @TempDir tempDir: Path
    ) {
        val aliasName = "some alias name"
        val validUntil = Instant.ofEpochMilli(50000)
        val cert = X509CertificateFactory.generateX509Certificate(validUntil = validUntil)
        val pathToKeystore = createKeyStoreFileContainingCertificates(mapOf(aliasName to cert), tempDir)

        val uut = ExpiringPkcs12(keyStoreName, pathToKeystore, password)

        expectThat(uut.expiringCertificates) hasSize 1
        expectThat(uut.expiringCertificates.first()) {
            get { subject.name } isEqualTo "$keyStoreName-$aliasName"
            get { subject.expirationDate!!.toInstant() } isEqualTo validUntil
        }
    }

    @Test
    fun `Given keystore that is not parsable, when keystore is created from file, Then ArtifactParsingException is thrown`(
        @TempDir tempDir: Path
    ) {
        val path = File("$tempDir/keystore")
        FileOutputStream(path).use { it.write("This is not a vali keystore".toByteArray()) }

        expectThrows<ArtifactParsingException> {
            ExpiringPkcs12(keyStoreName, path, password)
        }
    }

    private fun createKeyStoreFileContainingCertificates(aliasToCertificates: Map<String, X509Certificate>, tempDir: Path): File {
        val keyStore = KeyStore.getInstance(KeyStore.getDefaultType())
        keyStore.load(null, password.toCharArray())
        aliasToCertificates.forEach {
            keyStore.setCertificateEntry(it.key, it.value)
        }
        val path = File("$tempDir/keystore")
        FileOutputStream(path).use { keyStore.store(it, password.toCharArray()) }
        return path
    }
}
