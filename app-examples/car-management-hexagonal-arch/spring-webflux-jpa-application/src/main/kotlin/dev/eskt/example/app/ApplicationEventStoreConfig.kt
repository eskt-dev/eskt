package dev.eskt.example.app

import dev.eskt.store.api.EventStore
import dev.eskt.store.impl.pg.PostgresqlEventStore
import dev.eskt.store.test.w.car.CarStreamType
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.ComponentScan
import org.springframework.context.annotation.Configuration
import javax.sql.DataSource

@Configuration
@ComponentScan(
    basePackages = [
        "dev.eskt.arch.hex.adapter.spring",
    ],
)
class ApplicationEventStoreConfig {
    @Bean
    fun eventStore(dataSource: DataSource): EventStore {
        return PostgresqlEventStore(dataSource, "event") {
            registerStreamType(CarStreamType)
        }
    }
}
