package model

import io.micrometer.core.instrument.ImmutableTag
import java.util.Date

class ExpiredArtifact(override val name: String) : ExpiringArtifact {

    override val expirationDate: Date
        get() = Date(0)
    override val tags: Collection<ImmutableTag>
        get() = emptyList()
}
