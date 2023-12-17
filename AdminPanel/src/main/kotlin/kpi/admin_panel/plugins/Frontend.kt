package kpi.admin_panel.plugins

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.server.thymeleaf.*
import kpi.backend.*
import org.koin.ktor.ext.inject

fun Application.configureFrontend() {

    val viewerService by inject<ViewerService>()
    val orderService by inject<OrderService>()
    val ticketService by inject<TicketService>()

    routing {
        get("/viewers") {
            call.respond(
                ThymeleafContent(
                    "viewers", mapOf(
                        "viewers" to viewerService.readAll()
                    )
                )
            )
        }

        get("/viewers/{id}") {
            val id = call.parameters["id"]?.toInt() ?: throw IllegalArgumentException("Invalid ID")

            val viewerDetails = viewerService.read(viewerId = id)

            if (viewerDetails != null) {
                call.respond(
                    ThymeleafContent(
                        "viewer", mapOf(
                            "viewer" to viewerDetails
                        )
                    )
                )
            } else {
                call.respond(HttpStatusCode.NotFound)
            }
        }
    }
}
