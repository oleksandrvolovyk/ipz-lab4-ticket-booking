package kpi.plugins

import io.ktor.server.application.*
import kpi.backend.backendModule
import org.koin.core.context.GlobalContext
import org.koin.ktor.plugin.Koin
import org.koin.logger.slf4jLogger

fun Application.configureKoin() {
    install(Koin) {
        slf4jLogger()
        modules(backendModule)
        GlobalContext.startKoin(this)
    }
}