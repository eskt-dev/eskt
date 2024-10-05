package dev.eskt.example.app

import dev.eskt.arch.hex.adapter.common.BackoffStrategy
import dev.eskt.arch.hex.adapter.common.EventListenerExecutorConfig
import dev.eskt.store.api.EventStore
import dev.eskt.store.impl.pg.PostgresqlEventStore
import dev.eskt.store.test.w.car.CarStreamType
import dev.eskt.store.test.w.driver.DriverStreamType
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.ComponentScan
import org.springframework.context.annotation.Configuration
import org.springframework.jdbc.datasource.TransactionAwareDataSourceProxy
import javax.sql.DataSource
import kotlin.time.Duration.Companion.seconds

@Configuration
@ComponentScan(
    basePackages = [
        "dev.eskt.arch.hex.adapter.spring",
    ],
)
class ApplicationEventStoreConfig {
    @Bean
    fun eventStore(dataSource: DataSource): EventStore {
        val transactionAwareDataSource = TransactionAwareDataSourceProxy(dataSource)
        return PostgresqlEventStore(transactionAwareDataSource, "event") {
            registerStreamType(CarStreamType)
            registerStreamType(DriverStreamType)
        }
    }

    /**
     * This is how we can provide a customized [EventListenerExecutorConfig] bean instance.
     */
    @Bean
    fun eventListenerExecutorConfig(): EventListenerExecutorConfig {
        return EventListenerExecutorConfig(
            threadPoolName = "custom-event-listener-thread",
            errorBackoff = BackoffStrategy.Constant(30.seconds),
        )
    }
}
