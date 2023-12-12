package kpi.plugins

import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.server.thymeleaf.*
import kpi.backend.OrderService
import kpi.backend.Ticket
import kpi.backend.TicketService
import kpi.backend.ViewerService
import org.koin.ktor.ext.inject

data class TicketsFrontend(
    val movieTitle: String,
    val timesAndTickets: List<TimeAndTickets>,
)

data class TimeAndTickets(
    val time: Long,
    val tickets: List<TicketFrontend>
)

data class TicketFrontend(
    val ticketId: Int,
    val placeNumber: Int
)

fun Application.configureFrontend() {

    val viewerService by inject<ViewerService>()
    val orderService by inject<OrderService>()
    val ticketService by inject<TicketService>()

    fun List<Ticket>.toTicketsFrontend(): List<TicketsFrontend> {
        val groupedByMovie = groupBy { it.movieTitle }

        val ticketsFrontendList = groupedByMovie.map { (movieTitle, tickets) ->
            val timeAndTicketsList = tickets.groupBy { it.time }.map { (time, ticketsByTime) ->
                TimeAndTickets(
                    time = time,
                    tickets = ticketsByTime.map { TicketFrontend(it.ticketId, it.placeNumber) }
                )
            }

            TicketsFrontend(movieTitle, timeAndTicketsList)
        }

        return ticketsFrontendList
    }

    routing {
        get("/") {
            call.respond(
                ThymeleafContent("register", emptyMap())
            )
        }

        get("/tickets") {
            val tickets = ticketService.readAllWithOrderId(null).toTicketsFrontend()
            call.respond(
                ThymeleafContent(
                    "tickets",
                    mapOf(
                        "tickets" to tickets
                    )
                )
            )
        }
    }
}
