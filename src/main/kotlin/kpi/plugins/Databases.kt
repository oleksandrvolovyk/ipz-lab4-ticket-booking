package kpi.plugins

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import org.koin.ktor.ext.inject

fun Application.configureDatabases() {

    val viewerService by inject<ViewerService>()
    val orderService by inject<OrderService>()
    val ticketService by inject<TicketService>()

    routing {
        route("/api") {
            route("/viewers") {
                post {
                    val viewerDTO = call.receive<ViewerDTO>()
                    val id = viewerService.create(viewerDTO)
                    call.respond(HttpStatusCode.Created, id)
                }

                get {
                    call.respond(HttpStatusCode.OK, viewerService.readAll())
                }

                get("/{id}") {
                    val id = call.parameters["id"]?.toInt() ?: throw IllegalArgumentException("Invalid ID")
                    val viewer = viewerService.read(id)
                    if (viewer != null) {
                        call.respond(HttpStatusCode.OK, viewer)
                    } else {
                        call.respond(HttpStatusCode.NotFound)
                    }
                }

                get("/{id}/orders") {
                    val id = call.parameters["id"]?.toInt() ?: throw IllegalArgumentException("Invalid ID")
                    val viewer = viewerService.read(id)
                    if (viewer != null) {
                        call.respond(HttpStatusCode.OK, orderService.readAllWithViewerId(viewer.viewerId))
                    } else {
                        call.respond(HttpStatusCode.NotFound)
                    }
                }

                put("/{id}") {
                    val id = call.parameters["id"]?.toInt() ?: throw IllegalArgumentException("Invalid ID")
                    val viewerDTO = call.receive<ViewerDTO>()
                    viewerService.update(id, viewerDTO)
                    call.respond(HttpStatusCode.OK)
                }

                delete("/{id}") {
                    val id = call.parameters["id"]?.toInt() ?: throw IllegalArgumentException("Invalid ID")
                    viewerService.delete(id)
                    call.respond(HttpStatusCode.OK)
                }
            }

            route("/orders") {
                post {
                    val orderDTO = call.receive<OrderDTO>()
                    val id = orderService.create(orderDTO)
                    call.respond(HttpStatusCode.Created, id)
                }

                get {
                    call.respond(HttpStatusCode.OK, orderService.readAll())
                }

                get("/{id}") {
                    val id = call.parameters["id"]?.toInt() ?: throw IllegalArgumentException("Invalid ID")
                    val order = orderService.read(id)
                    if (order != null) {
                        call.respond(HttpStatusCode.OK, order)
                    } else {
                        call.respond(HttpStatusCode.NotFound)
                    }
                }

                put("/{id}") {
                    val id = call.parameters["id"]?.toInt() ?: throw IllegalArgumentException("Invalid ID")
                    val orderDTO = call.receive<OrderDTO>()
                    orderService.update(id, orderDTO)
                    call.respond(HttpStatusCode.OK)
                }

                delete("/{id}") {
                    val id = call.parameters["id"]?.toInt() ?: throw IllegalArgumentException("Invalid ID")
                    orderService.delete(id)
                    call.respond(HttpStatusCode.OK)
                }
            }

            route("/tickets") {
                post {
                    val ticketDTO = call.receive<TicketDTO>()
                    val id = ticketService.create(ticketDTO)
                    call.respond(HttpStatusCode.Created, id)
                }
                get {
                    call.respond(HttpStatusCode.OK, ticketService.readAll())
                }
                get("/{id}") {
                    val id = call.parameters["id"]?.toInt() ?: throw IllegalArgumentException("Invalid ID")
                    val ticket = ticketService.read(id)
                    if (ticket != null) {
                        call.respond(HttpStatusCode.OK, ticket)
                    } else {
                        call.respond(HttpStatusCode.NotFound)
                    }
                }
                put("/{id}") {
                    val id = call.parameters["id"]?.toInt() ?: throw IllegalArgumentException("Invalid ID")
                    val ticketDTO = call.receive<TicketDTO>()
                    ticketService.update(id, ticketDTO)
                    call.respond(HttpStatusCode.OK)
                }
                delete("/{id}") {
                    val id = call.parameters["id"]?.toInt() ?: throw IllegalArgumentException("Invalid ID")
                    ticketService.delete(id)
                    call.respond(HttpStatusCode.OK)
                }
            }
        }
    }
}
