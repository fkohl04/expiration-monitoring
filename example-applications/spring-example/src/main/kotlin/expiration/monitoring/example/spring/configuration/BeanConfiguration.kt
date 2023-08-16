package expiration.monitoring.example.spring.configuration

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import java.time.Clock

@Configuration
open class BeanConfiguration {

    @Bean
    open fun clock(): Clock = Clock.systemUTC()
}
