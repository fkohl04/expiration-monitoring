package utils

import java.math.BigInteger
import java.security.KeyPairGenerator
import java.security.PrivateKey
import java.security.cert.X509Certificate
import java.time.Instant
import java.time.temporal.ChronoUnit
import java.util.Date
import org.bouncycastle.asn1.x500.X500Name
import org.bouncycastle.asn1.x509.BasicConstraints
import org.bouncycastle.asn1.x509.Extension
import org.bouncycastle.asn1.x509.GeneralName
import org.bouncycastle.asn1.x509.GeneralNames
import org.bouncycastle.cert.jcajce.JcaX509CertificateConverter
import org.bouncycastle.cert.jcajce.JcaX509v3CertificateBuilder
import org.bouncycastle.operator.jcajce.JcaContentSignerBuilder

object X509CertificateFactory {

    fun generateX509Certificate(
        cnName: String = "CN",
        domain: String? = null,
        issuer: Pair<PrivateKey, X509Certificate>? = null,
        isCA: Boolean = false,
        validFrom: Instant = Instant.now(),
        validUntil: Instant = validFrom.plus(10 * 360.toLong(), ChronoUnit.DAYS),
    ): X509Certificate = generateCertKeyPair(cnName, domain, issuer, isCA, validFrom, validUntil).second

    @Suppress("LongMethod")
    fun generateCertKeyPair(
        cnName: String = "CN",
        domain: String? = null,
        issuer: Pair<PrivateKey, X509Certificate>? = null,
        isCA: Boolean = false,
        validFrom: Instant = Instant.now(),
        validUntil: Instant = validFrom.plus(10 * 360.toLong(), ChronoUnit.DAYS),
    ): Pair<PrivateKey, X509Certificate> {
        // Generate the key-pair with the official Java API's
        val keyGen = KeyPairGenerator.getInstance("RSA")
        val certKeyPair = keyGen.generateKeyPair()
        val name = X500Name("CN=$cnName")

        val serialNumber = BigInteger.valueOf(System.currentTimeMillis())

        // If there is no issuer, the certificate can be self-signed
        val issuerName: X500Name
        val issuerKey: PrivateKey
        if (issuer == null) {
            issuerName = name
            issuerKey = certKeyPair.private
        } else {
            issuerName = X500Name(issuer.second.subjectDN.name)
            issuerKey = issuer.first
        }

        val builder = JcaX509v3CertificateBuilder(
            issuerName,
            serialNumber,
            Date.from(validFrom), Date.from(validUntil),
            name, certKeyPair.public
        )

        if (isCA) {
            builder.addExtension(Extension.basicConstraints, true, BasicConstraints(isCA))
        }

        // Modern browsers demand the DNS name entry
        if (domain != null) {
            builder.addExtension(
                Extension.subjectAlternativeName, false,
                GeneralNames(
                    GeneralName(
                        GeneralName.dNSName,
                        domain
                    )
                )
            )
        }

        // Sign the certificate
        val signer = JcaContentSignerBuilder("SHA256WithRSA").build(issuerKey)
        val certHolder = builder.build(signer)
        val cert = JcaX509CertificateConverter().getCertificate(certHolder)
        return certKeyPair.private to cert

    }
}
