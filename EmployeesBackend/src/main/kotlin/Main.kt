package kpi

import com.mongodb.kotlin.client.coroutine.MongoClient
import com.mongodb.kotlin.client.coroutine.MongoDatabase
import kotlinx.coroutines.runBlocking

fun main() = runBlocking {
    val connectionString = "mongodb://mongo:mongo_pass@localhost:27018"
    val client = MongoClient.create(connectionString = connectionString)

    val databaseName = "moviedb"
    val db: MongoDatabase = client.getDatabase(databaseName = databaseName)

    val employeeService = EmployeeService(db)
    employeeService.create(EmployeeDTO("name", 20, "male"))
    println(employeeService.readAll())
}
