package kpi.backend

import org.jetbrains.exposed.sql.Database
import org.koin.dsl.module

val backendModule = module {
    single {
        Database.connect(
            url = "jdbc:postgresql://localhost:5433/moviedb",
            user = "postgres",
            driver = "org.postgresql.Driver",
            password = "postgres_pass"
        )
    }
    single { ViewerService(get()) }
    single { TicketService(get()) }
    single { OrderService(get(), get(), get()) }
}