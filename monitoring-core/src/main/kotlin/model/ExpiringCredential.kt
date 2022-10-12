package model

import io.micrometer.core.instrument.ImmutableTag
import java.util.Date

class ExpiringCredential (
    override val name: String,
    override val tags: Collection<ImmutableTag>,
    override val expirationDate: Date?
) : ExpiringArtifact
