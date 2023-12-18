package kpi.admin_panel.plugins

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.server.thymeleaf.*
import kpi.backend.*
import kpi.employees_backend.EmployeeService
import org.koin.ktor.ext.inject

fun Application.configureFrontend() {

    val viewerService by inject<ViewerService>()
    val orderService by inject<OrderService>()
    val ticketService by inject<TicketService>()
    val employeeService by inject<EmployeeService>()

    routing {
        get("/") {
            call.respond(ThymeleafContent("index" , emptyMap()))
        }

        get("/new-tickets") {
            call.respond(ThymeleafContent("new-tickets" , emptyMap()))
        }

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

        get("/tickets") {
            call.respond(
                ThymeleafContent(
                    "tickets", mapOf(
                        "tickets" to ticketService.readAll()
                    )
                )
            )
        }

        get("/tickets/{id}") {
            val id = call.parameters["id"]?.toInt() ?: throw IllegalArgumentException("Invalid ID")

            val ticketDetails = ticketService.read(ticketId = id)

            if (ticketDetails != null) {
                call.respond(
                    ThymeleafContent(
                        "ticket", mapOf(
                            "ticket" to ticketDetails
                        )
                    )
                )
            } else {
                call.respond(HttpStatusCode.NotFound)
            }
        }

        get("/orders/{id}") {
            val id = call.parameters["id"]?.toInt() ?: throw IllegalArgumentException("Invalid ID")

            val orderDetails = orderService.read(orderId = id)

            if (orderDetails != null) {
                call.respond(
                    ThymeleafContent(
                        "order", mapOf(
                            "order" to orderDetails
                        )
                    )
                )
            } else {
                call.respond(HttpStatusCode.NotFound)
            }
        }

        get("/employees") {
            call.respond(
                ThymeleafContent(
                    "employees", mapOf(
                        "employees" to employeeService.readAll()
                    )
                )
            )
        }

        get("/employees/{id}") {
            val id = call.parameters["id"] ?: throw IllegalArgumentException("Invalid ID")

            val employeeDetails = employeeService.read(employeeId = id)

            if (employeeDetails != null) {
                call.respond(
                    ThymeleafContent(
                        "employee", mapOf(
                            "employee" to employeeDetails
                        )
                    )
                )
            } else {
                call.respond(HttpStatusCode.NotFound)
            }
        }
    }
}
