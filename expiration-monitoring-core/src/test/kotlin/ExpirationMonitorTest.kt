import io.micrometer.core.instrument.ImmutableTag
import io.micrometer.core.instrument.MeterRegistry
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import io.mockk.verify
import java.time.Clock
import java.time.Instant
import java.time.ZoneId
import java.util.Date
import java.util.function.ToDoubleFunction
import java.util.stream.Stream
import model.ExpiringArtifact
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.Arguments.arguments
import org.junit.jupiter.params.provider.MethodSource
import strikt.api.expectThat
import strikt.assertions.isEqualTo

internal class ExpirationMonitorTest {
    private val fixedEpochMillis = 1337L
    private val clock = Clock.fixed(Instant.ofEpochMilli(fixedEpochMillis), ZoneId.systemDefault())
    private val registry = mockk<MeterRegistry>(relaxed = true)

    private val uut = ExpirationMonitor(clock, registry)

    private val artifactName = "someArtifactName"

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

    @ParameterizedTest
    @MethodSource("differenceToCurrentTimeToRemainingValidityInMs")
    fun `Given expiring artifact, When artifact is monitored, then remaining validity has expected size`(
        differenceToCurrentTime: Long,
        expectedRemainingValidity: Long
    ) {
        val slot = slot<ToDoubleFunction<ExpiringArtifact>>()
        val expiringArtifact = mockk<ExpiringArtifact>(relaxed = true)
        every { expiringArtifact.name } returns artifactName
        every { expiringArtifact.expirationDate } returns
                Date.from(Instant.ofEpochMilli(fixedEpochMillis + differenceToCurrentTime))
        every { registry.gauge("artifact.expiration", any(), expiringArtifact, capture(slot)) } returns
                expiringArtifact

        uut.monitorExpiringArtifact(expiringArtifact)

        expectThat(slot.captured.applyAsDouble(expiringArtifact)).isEqualTo(expectedRemainingValidity.toDouble())
    }

    companion object {
        @JvmStatic
        fun differenceToCurrentTimeToRemainingValidityInMs(): Stream<Arguments?>? {
            return Stream.of(
                arguments(Integer.MIN_VALUE, 0),
                arguments(-12, 0),
                arguments(-2, 0),
                arguments(-1, 0),
                arguments(0, 0),
                arguments(1, 1),
                arguments(2, 2),
                arguments(12, 12),
                arguments(Integer.MAX_VALUE, Integer.MAX_VALUE),
            )
        }
    }
}
