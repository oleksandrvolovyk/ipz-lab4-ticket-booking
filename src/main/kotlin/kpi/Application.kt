package kpi

import io.ktor.server.application.*
import kpi.plugins.*

fun main(args: Array<String>) {
    io.ktor.server.netty.EngineMain.main(args)
}

fun Application.module() {
    configureKoin()
    configureSerialization()
    configureDatabases()
    configureTemplating()
    configureMonitoring()
    configureRouting()
}
