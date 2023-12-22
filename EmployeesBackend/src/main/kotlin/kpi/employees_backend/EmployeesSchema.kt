package kpi.employees_backend

import com.mongodb.client.model.Filters.*
import com.mongodb.client.model.Updates
import com.mongodb.client.model.Updates.set
import com.mongodb.client.model.Updates.unset
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

    val highResPhotoId: String? = null,
    val mediumResPhotoId: String? = null,
    val lowResPhotoId: String? = null
)

@Serializable
data class EmployeeDTO(
    val fullName: String,
    val age: Int,
    val sex: String,

    val highResPhoto: ByteArray? = null,
    val mediumResPhoto: ByteArray? = null,
    val lowResPhoto: ByteArray? = null,

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
        var highResPhotoId: String? = null
        var mediumResPhotoId: String? = null
        var lowResPhotoId: String? = null

        if (employeeDTO.highResPhoto != null &&
            employeeDTO.mediumResPhoto != null &&
            employeeDTO.lowResPhoto != null &&
            employeeDTO.photoStorageMethod != null
        ) {

            highResPhotoId = pictureService.storePicture(
                pictureBytes = employeeDTO.highResPhoto,
                storageMethod = employeeDTO.photoStorageMethod
            )

            mediumResPhotoId = pictureService.storePicture(
                pictureBytes = employeeDTO.mediumResPhoto,
                storageMethod = employeeDTO.photoStorageMethod
            )

            lowResPhotoId = pictureService.storePicture(
                pictureBytes = employeeDTO.lowResPhoto,
                storageMethod = employeeDTO.photoStorageMethod
            )
        }

        val queryParam = eq(Employee::employeeId.name, employeeId)

        val updateParams = if (highResPhotoId != null && mediumResPhotoId != null && lowResPhotoId != null) {
            Updates.combine(
                set(Employee::fullName.name, employeeDTO.fullName),
                set(Employee::age.name, employeeDTO.age),
                set(Employee::sex.name, employeeDTO.sex),
                set(Employee::highResPhotoId.name, highResPhotoId),
                set(Employee::mediumResPhotoId.name, mediumResPhotoId),
                set(Employee::lowResPhotoId.name, lowResPhotoId),
            )
        } else {
            Updates.combine(
                set(Employee::fullName.name, employeeDTO.fullName),
                set(Employee::age.name, employeeDTO.age),
                set(Employee::sex.name, employeeDTO.sex),
                unset(Employee::highResPhotoId.name),
                unset(Employee::mediumResPhotoId.name),
                unset(Employee::lowResPhotoId.name),
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
