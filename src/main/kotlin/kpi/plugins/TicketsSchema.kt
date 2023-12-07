package kpi.plugins

import kotlinx.coroutines.Dispatchers
import kotlinx.serialization.Serializable
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction
import org.jetbrains.exposed.sql.transactions.transaction

@Serializable
data class Ticket(
    val ticketId: Int,
    val placeNumber: Int,
    val time: Long, // timestamp
    val movieTitle: String
)

@Serializable
data class TicketDTO(
    val placeNumber: Int,
    val time: Long, // timestamp
    val movieTitle: String
)

class TicketService(private val database: Database) {
    object Tickets : Table() {
        val ticket_id = integer("id").autoIncrement()
        val place_number = integer("place_number")
        val time = long("time")
        val movie_title = varchar("movie_title", length = 50)

        override val primaryKey = PrimaryKey(ticket_id)
    }

    init {
        transaction(database) {
            SchemaUtils.create(Tickets)
        }
    }

    suspend fun <T> dbQuery(block: suspend () -> T): T =
        newSuspendedTransaction(Dispatchers.IO) { block() }

    suspend fun create(ticketDTO: TicketDTO): Int = dbQuery {
        Tickets.insert {
            it[place_number] = ticketDTO.placeNumber
            it[time] = ticketDTO.time
            it[movie_title] = ticketDTO.movieTitle
        }[Tickets.ticket_id]
    }

    suspend fun readAll(): List<Ticket> = dbQuery {
        Tickets.selectAll()
            .map { it.toTicket() }
    }

    suspend fun read(ticketId: Int): Ticket? = dbQuery {
        Tickets.select { Tickets.ticket_id eq ticketId }
            .map { it.toTicket() }
            .singleOrNull()
    }

    suspend fun update(ticketId: Int, ticketDTO: TicketDTO) = dbQuery {
        Tickets.update({ Tickets.ticket_id eq ticketId }) {
            it[place_number] = ticketDTO.placeNumber
            it[time] = ticketDTO.time
            it[movie_title] = ticketDTO.movieTitle
        }
    }

    suspend fun delete(ticketId: Int) {
        dbQuery {
            Tickets.deleteWhere { ticket_id.eq(ticketId) }
        }
    }

    private fun ResultRow.toTicket() = Ticket(
        ticketId = this[Tickets.ticket_id],
        placeNumber = this[Tickets.place_number],
        time = this[Tickets.time],
        movieTitle = this[Tickets.movie_title]
    )
}