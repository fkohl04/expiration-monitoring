package expiration.monitoring.spring

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.context.properties.ConfigurationPropertiesScan
import org.springframework.boot.runApplication

@SpringBootApplication
@ConfigurationPropertiesScan("expiration.monitoring.spring.configuration")
open class TestApplication

fun main() {
    runApplication<TestApplication>()
}
