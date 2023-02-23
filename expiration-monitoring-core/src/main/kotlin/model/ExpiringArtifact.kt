package model

import io.micrometer.core.instrument.ImmutableTag
import java.util.Date

interface ExpiringArtifact {
    val name: String
    val expirationDate: Date?
    val tags: Collection<ImmutableTag>
}
