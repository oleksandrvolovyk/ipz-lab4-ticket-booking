package kpi.employees_backend

import io.ktor.http.*
import io.ktor.http.content.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.util.pipeline.*
import org.koin.ktor.ext.inject

fun Application.configureEmployeesAPI() {

    val employeeService by inject<EmployeeService>()
    val pictureService by inject<PictureService>()

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
                    updateEmployee(id, employeeService)
                }

                delete("/{id}") {
                    val id = call.parameters["id"] ?: throw IllegalArgumentException("Invalid ID")
                    employeeService.delete(id)
                    call.respond(HttpStatusCode.OK)
                }
            }

            route("/photos") {
                get {
                    val pictures = pictureService.readAllPictures()

                    call.respond(pictures)
                }

                get("/{id}") {
                    val id = call.parameters["id"] ?: throw IllegalArgumentException("Invalid name")

                    val pictureBytes = pictureService.readPicture(id)

                    if (pictureBytes != null) {
                        call.respondBytes(pictureBytes)
                    } else {
                        call.respond(HttpStatusCode.NotFound)
                    }
                }
            }
        }
    }
}

private suspend fun PipelineContext<Unit, ApplicationCall>.updateEmployee(
    id: String,
    employeeService: EmployeeService
) {
    val formParameters = call.receiveParameters()

    val fullName = formParameters["fullName"]
    val age = formParameters["age"]?.toIntOrNull()
    val sex = formParameters["sex"]

    if (fullName == null || age == null || sex == null) {
        call.respond(HttpStatusCode.BadRequest, "fullName == null || age == null || sex == null")
    } else {
        val photoStorageMethod = formParameters["photoStorageMethod"]

        var employeeDTO = EmployeeDTO(
            fullName = fullName,
            age = age,
            sex = sex
        )

        if (photoStorageMethod != null && photoStorageMethod == "database" || photoStorageMethod == "file") {
            // With photo
            val multipartData = call.receiveMultipart()

            var fileName: String? = null
            var fileBytes: ByteArray? = null
            multipartData.forEachPart { part ->
                when (part) {
                    is PartData.FileItem -> {
                        fileName = part.originalFileName

                        // TODO: Check file size
                        fileBytes = part.streamProvider().readBytes()
                    }

                    else -> {}
                }
                part.dispose()
            }

            if (fileBytes != null && fileName != null) {
                employeeDTO = employeeDTO.copy(photoFileName = fileName, photo = fileBytes)
                when (photoStorageMethod) {
                    "database" -> {
                        employeeDTO = employeeDTO.copy(photoStorageMethod = PictureService.StorageMethod.DATABASE)
                    }

                    "file" -> {
                        employeeDTO = employeeDTO.copy(photoStorageMethod = PictureService.StorageMethod.FILESYSTEM)
                    }
                }
            }
        }

        employeeService.update(id, employeeDTO)
        call.respond(HttpStatusCode.OK)
    }
}