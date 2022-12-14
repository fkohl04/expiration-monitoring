import io.micrometer.core.instrument.ImmutableTag
import io.micrometer.core.instrument.MeterRegistry
import java.lang.Double.max
import java.time.Clock
import java.util.Date
import model.ExpiredArtifact
import model.ExpiringArtifact
import org.slf4j.LoggerFactory

class ExpirationMonitor(
    private val clock: Clock,
    private val registry: MeterRegistry,
    private val globalTags: Collection<ImmutableTag> = emptyList(),
) {

    fun monitorExpiringArtifact(expiringArtifact: ExpiringArtifact) {
        logger.info("Monitoring expiration date of ${expiringArtifact.name}")

        registry.gauge(
            EXPIRATION_MONITORING_METRIC_NAME,
            listOf(ImmutableTag(ARTIFACT_NAME_TAG, expiringArtifact.name)) + expiringArtifact.tags + globalTags,
            expiringArtifact
        ) { calculateRemainingTimeInMs(it) }
    }

    /**
     * Use this method if the expiring artifact is not initialized.
     *
     * The second parameter needs to be a receiver to catch exceptions that may occur during initialization of the artifact.
     */
    fun receiveAndMonitorArtifact(name: String, getExpiringArtifact: () -> ExpiringArtifact) =
        executeOrMarkArtifactAsExpired(name) { monitorExpiringArtifact(getExpiringArtifact()) }

    fun receiveAndMonitorArtifacts(name: String, getExpiringArtifacts: () -> Collection<ExpiringArtifact>) =
        executeOrMarkArtifactAsExpired(name) {
            getExpiringArtifacts().map { monitorExpiringArtifact(it) }
        }

    private fun calculateRemainingTimeInMs(expiringArtifact: ExpiringArtifact) =
        expiringArtifact.expirationDate?.let { dateOfExpiry ->
            max((dateOfExpiry.time - Date.from(clock.instant()).time).toDouble(), 0.0)
        } ?: 0.0

    /**
     * If an exception occurs during configuration of the monitoring this should be as visible as possible. This method
     * can be used to safely initialize an artifact monitoring. In case of an exception the artifact will be marked as
     * expired in the service metrics.
     */
    private fun executeOrMarkArtifactAsExpired(name: String, executable: () -> Unit): Unit =
        runCatching { executable() }
            .getOrElse {
                logger.warn("Exception while trying to monitor $name.", it)
                monitorExpiringArtifact(ExpiredArtifact("$name substitute"))
            }

    companion object {
        private val logger = LoggerFactory.getLogger(ExpirationMonitor::class.java)
        const val EXPIRATION_MONITORING_METRIC_NAME = "artifact.expiration"
        const val ARTIFACT_NAME_TAG = "artifact.name"
    }
}
