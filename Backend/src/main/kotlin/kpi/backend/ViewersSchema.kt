package kpi.backend

import kotlinx.coroutines.Dispatchers
import kotlinx.serialization.Serializable
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction
import org.jetbrains.exposed.sql.transactions.transaction

@Serializable
data class Viewer(
    val viewerId: Int,
    val fullName: String,
    val age: Int,
    val sex: String
)

@Serializable
data class ViewerDTO(
    val fullName: String,
    val age: Int,
    val sex: String
)

class ViewerService(database: Database) {
    object Viewers : Table() {
        val viewer_id = integer("viewer_id").autoIncrement()

        val full_name = varchar("full_name", 255)
        val age = integer("age")
        val sex = varchar("sex", 10)

        override val primaryKey = PrimaryKey(viewer_id)
    }

    init {
        transaction(database) {
            SchemaUtils.create(Viewers)
        }
    }

    private suspend fun <T> dbQuery(block: suspend () -> T): T =
        newSuspendedTransaction(Dispatchers.IO) { block() }

    suspend fun create(viewerDTO: ViewerDTO): Int = dbQuery {
        Viewers.insert {
            it[full_name] = viewerDTO.fullName
            it[age] = viewerDTO.age
            it[sex] = viewerDTO.sex
        }[Viewers.viewer_id]
    }

    suspend fun readAll(): List<Viewer> = dbQuery {
        Viewers.selectAll()
            .map { it.toViewer() }
    }

    suspend fun read(viewerId: Int): Viewer? = dbQuery {
        Viewers.select { Viewers.viewer_id eq viewerId }
            .map { it.toViewer() }
            .singleOrNull()
    }

    suspend fun update(viewerId: Int, viewerDTO: ViewerDTO) = dbQuery {
        Viewers.update({ Viewers.viewer_id eq viewerId }) {
            it[full_name] = viewerDTO.fullName
            it[age] = viewerDTO.age
            it[sex] = viewerDTO.sex
        }
    }

    suspend fun delete(viewerId: Int) {
        dbQuery {
            Viewers.deleteWhere { viewer_id eq viewerId }
        }
    }

    private fun ResultRow.toViewer() = Viewer(
        viewerId = this[Viewers.viewer_id],
        fullName = this[Viewers.full_name],
        age = this[Viewers.age],
        sex = this[Viewers.sex]
    )
}