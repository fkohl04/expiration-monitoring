package utils

import java.math.BigInteger
import java.security.KeyPair
import java.security.KeyPairGenerator
import java.security.PrivateKey
import java.security.cert.X509Certificate
import java.time.Instant
import java.time.temporal.ChronoUnit
import java.util.Date
import org.bouncycastle.asn1.x500.X500Name
import org.bouncycastle.cert.jcajce.JcaX509CertificateConverter
import org.bouncycastle.cert.jcajce.JcaX509v3CertificateBuilder
import org.bouncycastle.operator.jcajce.JcaContentSignerBuilder

object X509CertificateFactory {

    fun generateX509Certificate(
        validFrom: Instant = Instant.now(),
        validUntil: Instant = validFrom.plus(10 * 360.toLong(), ChronoUnit.DAYS),
    ): X509Certificate {
        val keyPair: KeyPair = KeyPairGenerator.getInstance("RSA")
            .also { it.initialize(1024) }
            .generateKeyPair()

        val x509Builder = JcaX509v3CertificateBuilder(
            /* issuer = */ X500Name("C=DE, CN=test@example.com"),
            /* serial = */ BigInteger("900"),
            /* notBefore = */ Date.from(validFrom),
            /* notAfter = */ Date.from(validUntil),
            /* subject = */ X500Name("C=DE, CN=test@example.com"),
            /* publicKey = */ keyPair.public
        )

        return x509Builder.build(keyPair.private)
    }

    private fun JcaX509v3CertificateBuilder.build(privateKey: PrivateKey): X509Certificate =
        JcaX509CertificateConverter().getCertificate(
            build(
                JcaContentSignerBuilder("SHA256WITHRSA").build(privateKey)
            )
        )

}
