package kpi

import io.ktor.server.application.*
import kpi.plugins.*

fun main(args: Array<String>) {
    io.ktor.server.netty.EngineMain.main(args)
}

fun Application.module() {
    configureSerialization()
    configureDatabases()
    configureTemplating()
    configureMonitoring()
    configureRouting()
}
