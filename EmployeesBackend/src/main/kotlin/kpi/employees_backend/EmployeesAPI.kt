package kpi.employees_backend

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import org.koin.ktor.ext.inject

fun Application.configureEmployeesAPI() {

    val employeeService by inject<EmployeeService>()

    routing {
        route("/api") {
            route("/employees") {
                get {
                    val fromAge = call.request.queryParameters["from_age"]?.toIntOrNull()
                    val toAge = call.request.queryParameters["to_age"]?.toIntOrNull()

                    if (fromAge != null && toAge != null) {
                        if (fromAge > toAge) {
                            call.respond(HttpStatusCode.BadRequest, "from_age > to_age")
                        }
                        call.respond(HttpStatusCode.OK, employeeService.readAllInAgeRange(fromAge, toAge))
                    } else {
                        call.respond(HttpStatusCode.OK, employeeService.readAll())
                    }
                }

                get("/{id}") {
                    val id = call.parameters["id"] ?: throw IllegalArgumentException("Invalid ID")
                    val employee = employeeService.read(id)
                    if (employee != null) {
                        call.respond(HttpStatusCode.OK, employee)
                    } else {
                        call.respond(HttpStatusCode.NotFound)
                    }
                }

                post {
                    val employeeDTO = call.receive<EmployeeDTO>()
                    val id = employeeService.create(employeeDTO)
                    call.respond(HttpStatusCode.Created, id)
                }

                put("/{id}") {
                    val id = call.parameters["id"] ?: throw IllegalArgumentException("Invalid ID")
                    val employeeDTO = call.receive<EmployeeDTO>()
                    employeeService.update(id, employeeDTO)
                    call.respond(HttpStatusCode.OK)
                }

                delete("/{id}") {
                    val id = call.parameters["id"] ?: throw IllegalArgumentException("Invalid ID")
                    employeeService.delete(id)
                    call.respond(HttpStatusCode.OK)
                }
            }
        }
    }
}
