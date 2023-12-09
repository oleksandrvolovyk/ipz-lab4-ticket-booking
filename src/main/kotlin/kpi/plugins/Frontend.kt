package kpi.plugins

import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.server.thymeleaf.*
import org.koin.ktor.ext.inject

fun Application.configureFrontend() {

    val viewerService by inject<ViewerService>()
    val orderService by inject<OrderService>()
    val ticketService by inject<TicketService>()

    routing {
        get("/") {
            call.respond(
                ThymeleafContent("register", emptyMap())
            )
        }

        get("/tickets") {
            call.respond(
                ThymeleafContent(
                    "tickets",
                    mapOf(
                        "tickets" to ticketService.readAll().filter { it.orderId == null }
                    )
                )
            )
        }
    }
}
