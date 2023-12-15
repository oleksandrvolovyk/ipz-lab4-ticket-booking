package kpi.employees_backend

import com.mongodb.kotlin.client.coroutine.MongoClient
import org.koin.dsl.module

val employeesBackendModule = module {
    single {
        val connectionString = "mongodb://${System.getenv("MONGO_USER")}:${System.getenv("MONGO_PASS")}@${System.getenv("MONGO_HOST")}:${System.getenv("MONGO_PORT")}"
        val client = MongoClient.create(connectionString = connectionString)

        val databaseName = System.getenv("MONGO_DB_NAME")
        return@single client.getDatabase(databaseName = databaseName)
    }
    single {
        EmployeeService(get())
    }
}