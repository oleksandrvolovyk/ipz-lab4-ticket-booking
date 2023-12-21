package kpi.employees_backend

import com.mongodb.kotlin.client.coroutine.MongoClient
import org.koin.dsl.module
import kotlin.system.exitProcess

// TODO: Move to env vars?
const val PICTURES_DIRECTORY = "uploads"
val ALLOWED_PHOTO_EXTENSIONS = arrayOf("jpg", "png", "gif")

val employeesBackendModule = module {
    single {
        val host = System.getenv("MONGO_HOST")
        val port = System.getenv("MONGO_PORT")
        val dbName = System.getenv("MONGO_DB_NAME")
        val username = System.getenv("MONGO_USER")
        val password = System.getenv("MONGO_PASS")

        if (host == null || port == null || dbName == null || username == null || password == null) {
            println("Required environment variable(s) not found!")
            println("MONGO_HOST = $host")
            println("MONGO_PORT = $port")
            println("MONGO_DB_NAME = $dbName")
            println("MONGO_USER = $username")
            println("MONGO_PASS = $password")
            exitProcess(1)
        }

        val connectionString = "mongodb://$username:$password@$host:$port"
        val client = MongoClient.create(connectionString = connectionString)

        return@single client.getDatabase(databaseName = dbName)
    }
    single {
        PictureService(
            database = get(),
            pictureDirectory = PICTURES_DIRECTORY,
            allowedPictureExtensions = ALLOWED_PHOTO_EXTENSIONS
        )
    }
    single {
        EmployeeService(database = get(), pictureService = get())
    }
}