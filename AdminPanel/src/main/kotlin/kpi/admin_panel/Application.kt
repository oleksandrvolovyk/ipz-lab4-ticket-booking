package kpi.admin_panel

import io.ktor.server.application.*
import kpi.backend.configureDatabases
import kpi.admin_panel.plugins.*
import kpi.configureEmployeesAPI

fun main(args: Array<String>) {
    io.ktor.server.netty.EngineMain.main(args)
}

fun Application.module() {
    configureKoin()
    configureSerialization()
    configureDatabases()
    configureEmployeesAPI()
    configureTemplating()
    configureMonitoring()
    configureRouting()
    configureFrontend()
}
