package model

import io.micrometer.core.instrument.ImmutableTag
import java.util.Date

/**
 * This class represents an artifact that was provided to this library, but that is not parsable. To make this unwanted
 * state as visible as possible such an artifact is monitored as parsable artifacts. Furthermore, it is marked as
 * expired and the actual artifact name is prefixed with 'Parsing Error:'.
 */
class NotParsableArtifact(referenceArtifactName: String) : ExpiringArtifact {

    override val expirationDate: Date
        get() = Date(0)
    override val tags: Collection<ImmutableTag>
        get() = emptyList()

    override val name = "Parsing Error: $referenceArtifactName"
}
