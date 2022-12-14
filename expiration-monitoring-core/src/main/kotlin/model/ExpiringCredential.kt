package model

import io.micrometer.core.instrument.ImmutableTag
import java.util.Date

class ExpiringCredential (
    override val name: String,
    override val expirationDate: Date?,
    override val tags: Collection<ImmutableTag> = emptyList(),
) : ExpiringArtifact
