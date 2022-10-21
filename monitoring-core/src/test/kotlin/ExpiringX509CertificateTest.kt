import java.io.File
import java.io.FileOutputStream
import java.nio.file.Path
import java.time.Instant
import java.util.stream.Stream
import model.ExpiringX509Certificate
import org.junit.jupiter.api.io.TempDir
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource
import strikt.api.expectThat
import strikt.assertions.isEqualTo
import strikt.assertions.isNotNull
import utils.X509CertificateFactory

internal class ExpiringX509CertificateTest {

    @ParameterizedTest
    @MethodSource("longStream")
    fun `Given X509Certificate as file, When certificate is monitored, then expiration date is as expected`(
        input: Long,
        @TempDir tempDir: Path
    ) {
        val cert = X509CertificateFactory.generateX509Certificate(validUntil = Instant.ofEpochMilli(input))
        val path = File("$tempDir/test.pfx")
        FileOutputStream(path).use { it.write(cert.encoded) }

        val uut = ExpiringX509Certificate("someName", path)

        expectThat(uut.expirationDate)
            .isNotNull()
            .get { this.time }.isEqualTo((input / 1000L) * 1000L)
    }

    @ParameterizedTest
    @MethodSource("longStream")
    fun `Given X509Certificate, When certificate is monitored, then expiration date is as expected`(input: Long) {
        val cert = X509CertificateFactory.generateX509Certificate(validUntil = Instant.ofEpochMilli(input))

        val uut = ExpiringX509Certificate("someName", cert)

        expectThat(uut.expirationDate)
            .isNotNull()
            .get { this.time }.isEqualTo((input / 1000L) * 1000L)
    }

    companion object {
        @JvmStatic
        fun longStream(): Stream<Arguments?>? {
            return Stream.of(
                Arguments.arguments(0),
                Arguments.arguments(1L),
                Arguments.arguments(2L),
                Arguments.arguments(121234321234L),
                Arguments.arguments(Integer.MAX_VALUE.toLong()),
            )
        }
    }
}
