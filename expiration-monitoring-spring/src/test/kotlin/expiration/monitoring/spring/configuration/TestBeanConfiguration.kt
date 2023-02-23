package expiration.monitoring.spring.configuration

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import java.time.Clock

@Configuration
open class TestBeanConfiguration {

    @Bean
    open fun clock(): Clock = Clock.systemUTC()
}
