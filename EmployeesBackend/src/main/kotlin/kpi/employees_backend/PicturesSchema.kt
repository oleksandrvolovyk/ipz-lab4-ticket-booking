package kpi.employees_backend

import com.mongodb.client.model.Filters
import com.mongodb.kotlin.client.coroutine.MongoDatabase
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.toList
import kotlinx.serialization.Transient
import org.bson.codecs.pojo.annotations.BsonId
import org.bson.types.ObjectId
import java.io.File
import java.util.*
import kotlin.io.encoding.Base64
import kotlin.io.encoding.ExperimentalEncodingApi

data class Picture(
    @BsonId @Transient
    val id: ObjectId = ObjectId(),
    val pictureId: String,
    val base64EncodedPicture: String?,
    val pictureFilePath: String?
)

class PictureService(
    database: MongoDatabase,
    private val pictureDirectory: String,
    private val allowedPictureExtensions: Array<String>
) {

    enum class StorageMethod {
        DATABASE,
        FILESYSTEM
    }

    private val collection = database.getCollection<Picture>(collectionName = "photo")

    @OptIn(ExperimentalEncodingApi::class)
    suspend fun readAllPictures(): List<ByteArray?> {
        val pictures = collection.find<Picture>().toList()

        return pictures.map { picture ->
            if (picture.base64EncodedPicture != null) {
                Base64.decode(picture.base64EncodedPicture)
            } else if (picture.pictureFilePath != null) {
                File(picture.pictureFilePath).readBytes()
            } else {
                null
            }
        }
    }

    @OptIn(ExperimentalEncodingApi::class)
    suspend fun readPicture(pictureId: String): ByteArray? {
        val queryParams = Filters.eq(Picture::pictureId.name, pictureId)

        val picture = collection.find<Picture>(queryParams).limit(1).firstOrNull()

        if (picture == null) {
            return picture
        }

        return if (picture.base64EncodedPicture != null) {
            Base64.decode(picture.base64EncodedPicture)
        } else if (picture.pictureFilePath != null) {
            File(picture.pictureFilePath).readBytes()
        } else {
            null
        }
    }

    suspend fun storePicture(pictureName: String, pictureBytes: ByteArray, storageMethod: StorageMethod): String {
        if (!allowedFileExtension(pictureName, allowedPictureExtensions)) {
            // TODO: More robust file type checking
            throw IllegalArgumentException(
                "${pictureName.fileExtension} is not allowed! " +
                        "Allowed file extension are: $allowedPictureExtensions"
            )
        }

        return when (storageMethod) {
            StorageMethod.DATABASE -> storePictureInDatabase(pictureName, pictureBytes)
            StorageMethod.FILESYSTEM -> storePictureInFilesystem(pictureName, pictureBytes)
        }
    }

    @OptIn(ExperimentalEncodingApi::class)
    private suspend fun storePictureInDatabase(pictureName: String, pictureBytes: ByteArray): String {
        val base64EncodedPicture = Base64.encode(pictureBytes)

        val pictureId = UUID.randomUUID().toString()

        collection.insertOne(
            Picture(
                pictureId = pictureId,
                base64EncodedPicture = base64EncodedPicture,
                pictureFilePath = null
            )
        )

        return pictureId
    }

    private suspend fun storePictureInFilesystem(pictureName: String, pictureBytes: ByteArray): String {
        val pictureId = UUID.randomUUID().toString()

        val localFileName = "$pictureDirectory/$pictureId.${pictureName.fileExtension}"
        File(localFileName).writeBytes(pictureBytes)

        collection.insertOne(
            Picture(
                pictureId = pictureId,
                base64EncodedPicture = null,
                pictureFilePath = localFileName
            )
        )

        return pictureId
    }

    private val String.fileExtension: String
        get() = this.substringAfterLast(".").lowercase(Locale.getDefault())

    private fun allowedFileExtension(filename: String, allowedFileExtensions: Array<String>): Boolean =
        filename.substringAfterLast(".") in allowedFileExtensions
}