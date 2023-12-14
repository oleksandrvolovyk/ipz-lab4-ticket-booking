package kpi

import com.mongodb.client.model.Filters.eq
import com.mongodb.client.model.Updates
import com.mongodb.client.model.Updates.set
import com.mongodb.kotlin.client.coroutine.MongoDatabase
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.toList
import org.bson.BsonObjectId
import org.bson.codecs.pojo.annotations.BsonId
import org.bson.types.ObjectId
import java.util.UUID

data class Employee(
    @BsonId
    val id: ObjectId,
    val fullName: String,
    val age: Int,
    val sex: String,
    val employeeId: String
)

data class EmployeeDTO(
    val fullName: String,
    val age: Int,
    val sex: String
)

class EmployeeService(private val database: MongoDatabase) {
    private val collection = database.getCollection<Employee>(collectionName = "employee")
    suspend fun create(employeeDTO: EmployeeDTO): String {
        val item = Employee(
            id = ObjectId(),
            fullName = employeeDTO.fullName,
            age = employeeDTO.age,
            sex = employeeDTO.sex,
            employeeId = UUID.randomUUID().toString()
        )

        return (collection.insertOne(item).insertedId as BsonObjectId).value.toHexString()
    }

    suspend fun read(employeeId: String): Employee {
        val queryParams = eq(Employee::employeeId.name, employeeId)

        return collection.find<Employee>(queryParams).limit(1).first()
    }

    suspend fun readAll(): List<Employee> {
        return collection.find<Employee>().toList()
    }

    suspend fun update(employeeId: String, employeeDTO: EmployeeDTO) {
        val queryParam = eq(Employee::employeeId.name, employeeId)
        val updateParams = Updates.combine(
            set(Employee::fullName.name, employeeDTO.fullName),
            set(Employee::age.name, employeeDTO.age),
            set(Employee::sex.name, employeeDTO.sex)
        )
        collection.updateOne(filter = queryParam, update = updateParams)
    }

    suspend fun delete(employeeId: String) {
        val queryParams = eq(Employee::employeeId.name, employeeId)

        collection.findOneAndDelete(filter = queryParams)
    }
}
