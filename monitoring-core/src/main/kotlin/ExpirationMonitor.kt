import io.micrometer.core.instrument.ImmutableTag
import io.micrometer.core.instrument.MeterRegistry
import java.time.Clock
import java.util.Date
import model.ExpiringArtifact
import org.slf4j.LoggerFactory

class ExpirationMonitor(
    private val clock: Clock,
    private val registry: MeterRegistry
) {

    fun monitorExpiringArtifact(expiringArtifact: ExpiringArtifact) {
        logger.info("Monitoring expiration date of ${expiringArtifact.name}")

        registry.gauge(
            EXPIRATION_MONITORING_METRIC_NAME,
            listOf(ImmutableTag(ARTIFACT_NAME_TAG, expiringArtifact.name)) + expiringArtifact.tags,
            expiringArtifact
        ) { calculateRemainingTimeInMs(it) }
    }

    private fun calculateRemainingTimeInMs(expiringArtifact: ExpiringArtifact) =
        expiringArtifact.expirationDate?.let { dateOfExpiry ->
            (dateOfExpiry.time - Date.from(clock.instant()).time).toDouble()
        } ?: 0.0

    companion object {
        private val logger = LoggerFactory.getLogger(ExpirationMonitor::class.java)
        const val EXPIRATION_MONITORING_METRIC_NAME = "artifact.expiration"
        const val ARTIFACT_NAME_TAG = "name"
    }
}
