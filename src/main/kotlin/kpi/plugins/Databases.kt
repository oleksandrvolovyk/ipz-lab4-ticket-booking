package kpi.plugins

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import org.koin.ktor.ext.inject

fun Application.configureDatabases() {

    val ticketService by inject<TicketService>()

    routing {
        // Create ticket
        post("/tickets") {
            val ticketDTO = call.receive<TicketDTO>()
            val id = ticketService.create(ticketDTO)
            call.respond(HttpStatusCode.Created, id)
        }
        // Read all tickets
        get("/tickets") {
            call.respond(HttpStatusCode.OK, ticketService.readAll())
        }
        // Read ticket
        get("/tickets/{id}") {
            val id = call.parameters["id"]?.toInt() ?: throw IllegalArgumentException("Invalid ID")
            val ticket = ticketService.read(id)
            if (ticket != null) {
                call.respond(HttpStatusCode.OK, ticket)
            } else {
                call.respond(HttpStatusCode.NotFound)
            }
        }
        // Update ticket
        put("/tickets/{id}") {
            val id = call.parameters["id"]?.toInt() ?: throw IllegalArgumentException("Invalid ID")
            val ticketDTO = call.receive<TicketDTO>()
            ticketService.update(id, ticketDTO)
            call.respond(HttpStatusCode.OK)
        }
        // Delete ticket
        delete("/tickets/{id}") {
            val id = call.parameters["id"]?.toInt() ?: throw IllegalArgumentException("Invalid ID")
            ticketService.delete(id)
            call.respond(HttpStatusCode.OK)
        }
    }
}
