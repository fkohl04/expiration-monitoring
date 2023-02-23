import exception.ArtifactParsingException
import java.io.File
import java.io.FileOutputStream
import java.nio.file.Path
import java.time.Instant
import model.ExpiringX509Certificate
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import strikt.api.expectThat
import strikt.api.expectThrows
import strikt.assertions.isEqualTo
import strikt.assertions.isNotNull
import utils.X509CertificateFactory

internal class ExpiringX509CertificateTest {

    private val remainingTime = 121234321234L

    @Test
    fun `Given X509Certificate as file, When certificate is monitored, then expiration date is as expected`(
        @TempDir tempDir: Path
    ) {
        val cert = X509CertificateFactory.generateX509Certificate(validUntil = Instant.ofEpochMilli(remainingTime))
        val path = File("$tempDir/test.pfx")
        FileOutputStream(path).use { it.write(cert.encoded) }

        val uut = ExpiringX509Certificate("someName", path)

        expectThat(uut.expirationDate)
            .isNotNull()
            .get { this.time }.isEqualTo((remainingTime / 1000L) * 1000L)
    }

    @Test
    fun `Given X509Certificate, When certificate is monitored, then expiration date is as expected`() {
        val cert = X509CertificateFactory.generateX509Certificate(validUntil = Instant.ofEpochMilli(remainingTime))

        val uut = ExpiringX509Certificate("someName", cert)

        expectThat(uut.expirationDate)
            .isNotNull()
            .get { this.time }.isEqualTo((remainingTime / 1000L) * 1000L)
    }

    @Test
    fun `Given non parsable X509Certificate, When certificate is monitored, then ArtifactParsingException is thrown`() {
        expectThrows<ArtifactParsingException> {
            ExpiringX509Certificate("someName", "This is not parsable content")
        }
    }
}
