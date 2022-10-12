import io.micrometer.core.instrument.ImmutableTag
import io.micrometer.core.instrument.MeterRegistry
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import java.time.Clock
import java.time.Instant
import java.time.ZoneId
import model.ExpiringArtifact
import org.junit.jupiter.api.Test

internal class ExpirationMonitorTest {

    private val clock = Clock.fixed(Instant.ofEpochMilli(0), ZoneId.systemDefault())
    private val registry = mockk<MeterRegistry>(relaxed = true)

    private val uut = ExpirationMonitor(clock, registry)

    private val artifactName = "someArtifactName"

    // Todo Add tests for monitoring calculation

    @Test
    fun `Given expiring artifact, When artifact is monitored, then meter registry is called with proper values`() {
        val expiringArtifact = mockk<ExpiringArtifact>(relaxed = true)
        every { expiringArtifact.name } returns artifactName

        uut.monitorExpiringArtifact(expiringArtifact)

        verify {
            registry.gauge(
                "artifact.expiration",
                listOf(ImmutableTag("artifact.name", artifactName)),
                expiringArtifact,
                any()
            )
        }
    }

    @Test
    fun `Given expiring artifact with additional tags, When artifact is monitored, then meter registry is called with proper values and additional tags`() {
        val artifactsTags = listOf(
            ImmutableTag("additionalTag1Key", "additionalTag1Value"),
            ImmutableTag("additionalTag2Key", "additionalTag2Value")
        )
        val expiringArtifact = mockk<ExpiringArtifact>(relaxed = true)
        every { expiringArtifact.name } returns artifactName
        every { expiringArtifact.tags } returns artifactsTags

        uut.monitorExpiringArtifact(expiringArtifact)

        verify {
            registry.gauge(
                "artifact.expiration",
                listOf(ImmutableTag("artifact.name", artifactName)) + artifactsTags,
                expiringArtifact,
                any()
            )
        }
    }

    @Test
    fun `Given expiration monitor with global tags, When an artifact is monitored, then meter registry is called with proper values and global tags`() {
        val globalTags = listOf(
            ImmutableTag("additionalGlobalTag1Key", "additionalGlobalTag1Value"),
            ImmutableTag("additionalGlobalTag2Key", "additionalGlobalTag2Value")
        )
        val monitorWithGlobalTags = ExpirationMonitor(clock, registry, globalTags)
        val expiringArtifact = mockk<ExpiringArtifact>(relaxed = true)
        every { expiringArtifact.name } returns artifactName

        monitorWithGlobalTags.monitorExpiringArtifact(expiringArtifact)

        verify {
            registry.gauge(
                "artifact.expiration",
                listOf(ImmutableTag("artifact.name", artifactName)) + globalTags,
                expiringArtifact,
                any()
            )
        }
    }
}
