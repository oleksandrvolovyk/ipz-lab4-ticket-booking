package kpi.backend

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.plugins.*
import io.ktor.server.plugins.doublereceive.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import org.koin.ktor.ext.inject

fun Application.configureDatabases() {

    install(DoubleReceive)

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

                delete("/{id}/tickets/{ticketId}") {
                    val orderId = call.parameters["id"]?.toInt() ?: throw IllegalArgumentException("Invalid Order ID")
                    val ticketId =
                        call.parameters["ticketId"]?.toInt() ?: throw IllegalArgumentException("Invalid Ticket ID")

                    val order = orderService.read(orderId)
                    val ticket = ticketService.read(ticketId)

                    if (order == null) {
                        call.respond(HttpStatusCode.NotFound, "Order not found")
                    } else if (ticket == null) {
                        call.respond(HttpStatusCode.NotFound, "Ticket not found")
                    } else if (ticket.orderId != orderId) {
                        call.respond(HttpStatusCode.BadRequest, "Ticket not in this order")
                    } else {
                        ticketService.updateTicketOrderId(ticketId, null)
                        call.respond(HttpStatusCode.OK)
                    }
                }
            }

            route("/tickets") {
                post {
                    try {
                        val ticketDTO = call.receive<TicketDTO>()
                        val id = ticketService.create(ticketDTO)
                        call.respond(HttpStatusCode.Created, id)
                    } catch (_: BadRequestException) {
                    }
                    try {
                        val ticketDTOs = call.receive<List<TicketDTO>>()
                        val ids = ticketService.batchCreate(ticketDTOs)
                        call.respond(HttpStatusCode.Created, ids)
                    } catch (_: BadRequestException) {
                    }
                    call.respond(
                        HttpStatusCode.BadRequest,
                        "Failed to parse request body to TicketDTO or List<TicketDTO>"
                    )
                }
                get {
                    val movieTitle = call.request.queryParameters["movieTitle"]
                    val timestamp = call.request.queryParameters["timestamp"]?.toLongOrNull()
                    val orderId = call.request.queryParameters["orderId"]?.toIntOrNull()

                    val fromTime = call.request.queryParameters["from"]?.toLongOrNull()
                    val toTime = call.request.queryParameters["to"]?.toLongOrNull()

                    if (movieTitle != null && timestamp != null) {
                        call.respond(
                            HttpStatusCode.OK,
                            ticketService.readAllWithMovieTitleAndTimeAndOrderId(movieTitle, timestamp, orderId)
                        )
                    } else if (fromTime != null && toTime != null) {
                        if (fromTime > toTime) {
                            call.respond(HttpStatusCode.BadRequest, "from > to")
                        }
                        call.respond(
                            HttpStatusCode.OK,
                            ticketService.readAllInTimeRange(fromTime, toTime)
                        )
                    } else {
                        call.respond(HttpStatusCode.OK, ticketService.readAll())
                    }
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
                    if ((ticketDTO.orderId != null && orderService.exists(ticketDTO.orderId)) || ticketDTO.orderId == null) {
                        ticketService.update(id, ticketDTO)
                        call.respond(HttpStatusCode.OK)
                    } else {
                        call.respond(HttpStatusCode.NotFound, "Order with order_id ${ticketDTO.orderId} not found")
                    }
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
