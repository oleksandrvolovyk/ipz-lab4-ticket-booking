package kpi

import com.mongodb.kotlin.client.coroutine.MongoClient
import org.koin.dsl.module

val employeesBackendModule = module {
    single {
        val connectionString = "mongodb://mongo:mongo_pass@localhost:27018"
        val client = MongoClient.create(connectionString = connectionString)

        val databaseName = "moviedb"
        return@single client.getDatabase(databaseName = databaseName)
    }
    single {
        EmployeeService(get())
    }
}