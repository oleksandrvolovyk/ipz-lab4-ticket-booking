package kpi.backend

import org.jetbrains.exposed.sql.Database
import org.koin.dsl.module

val backendModule = module {
    single {
        Database.connect(
            url = "jdbc:postgresql://${System.getenv("POSTGRES_HOST")}:${System.getenv("POSTGRES_PORT")}/" +
                    System.getenv("POSTGRES_DB_NAME"),
            user = System.getenv("POSTGRES_USER"),
            driver = "org.postgresql.Driver",
            password = System.getenv("POSTGRES_PASS")
        )
    }
    single { ViewerService(get()) }
    single { TicketService(get()) }
    single { OrderService(get(), get(), get()) }
}