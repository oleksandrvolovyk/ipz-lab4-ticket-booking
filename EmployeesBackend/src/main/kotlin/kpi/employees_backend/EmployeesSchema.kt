package kpi.employees_backend

import com.mongodb.client.model.Filters.*
import com.mongodb.client.model.Updates
import com.mongodb.client.model.Updates.set
import com.mongodb.kotlin.client.coroutine.MongoDatabase
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.toList
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient
import org.bson.codecs.pojo.annotations.BsonId
import org.bson.types.ObjectId
import java.util.*

@Serializable
data class Employee(
    @BsonId @Transient
    val id: ObjectId = ObjectId(),
    val fullName: String,
    val age: Int,
    val sex: String,
    val employeeId: String,
    val photoId: String? = null
)

@Serializable
data class EmployeeDTO(
    val fullName: String,
    val age: Int,
    val sex: String,
    val photoFileName: String? = null,
    val photo: ByteArray? = null,
    val photoStorageMethod: PictureService.StorageMethod? = null
)

class EmployeeService(database: MongoDatabase, private val pictureService: PictureService) {
    private val collection = database.getCollection<Employee>(collectionName = "employee")
    suspend fun create(employeeDTO: EmployeeDTO): String {
        val employeeId = UUID.randomUUID().toString()
        val item = Employee(
            fullName = employeeDTO.fullName,
            age = employeeDTO.age,
            sex = employeeDTO.sex,
            employeeId = employeeId
        )

        collection.insertOne(item)

        return employeeId
    }

    suspend fun read(employeeId: String): Employee? {
        val queryParams = eq(Employee::employeeId.name, employeeId)

        return collection.find<Employee>(queryParams).limit(1).firstOrNull()
    }

    suspend fun readAll(): List<Employee> {
        return collection.find<Employee>().toList()
    }

    suspend fun readAllInAgeRange(from: Int, to: Int): List<Employee> {
        val queryParams = and(
            gte(Employee::age.name, from),
            lte(Employee::age.name, to)
        )

        return collection.find<Employee>(queryParams).toList()
    }

    suspend fun update(
        employeeId: String,
        employeeDTO: EmployeeDTO
    ) {
        var photoId: String? = null
        if (employeeDTO.photoFileName != null && employeeDTO.photo != null && employeeDTO.photoStorageMethod != null) {
            photoId = pictureService.storePicture(
                pictureName = employeeDTO.photoFileName,
                pictureBytes = employeeDTO.photo,
                storageMethod = employeeDTO.photoStorageMethod
            )
        }

        val queryParam = eq(Employee::employeeId.name, employeeId)
        val updateParams = if (photoId != null) {
            Updates.combine(
                set(Employee::fullName.name, employeeDTO.fullName),
                set(Employee::age.name, employeeDTO.age),
                set(Employee::sex.name, employeeDTO.sex),
                set(Employee::photoId.name, photoId)
            )
        } else {
            Updates.combine(
                set(Employee::fullName.name, employeeDTO.fullName),
                set(Employee::age.name, employeeDTO.age),
                set(Employee::sex.name, employeeDTO.sex)
            )
        }

        collection.updateOne(filter = queryParam, update = updateParams)
    }

    suspend fun delete(employeeId: String) {
        val queryParams = eq(Employee::employeeId.name, employeeId)

        // TODO: Delete photo file(if it was stored as file)

        collection.findOneAndDelete(filter = queryParams)
    }
}
