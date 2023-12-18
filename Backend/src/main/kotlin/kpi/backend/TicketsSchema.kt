package kpi.backend

import kotlinx.coroutines.Dispatchers
import kotlinx.serialization.Serializable
import kpi.backend.TicketService.Tickets.movie_title
import kpi.backend.TicketService.Tickets.order_id
import kpi.backend.TicketService.Tickets.place_number
import kpi.backend.TicketService.Tickets.ticket_id
import kpi.backend.TicketService.Tickets.time
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction
import org.jetbrains.exposed.sql.transactions.transaction

@Serializable
data class Ticket(
    val ticketId: Int,
    val placeNumber: Int,
    val time: Long, // timestamp
    val movieTitle: String,
    val orderId: Int?
)

@Serializable
data class TicketDTO(
    val placeNumber: Int,
    val time: Long, // timestamp
    val movieTitle: String,
    val orderId: Int? = null
)

class TicketService(database: Database) {
    object Tickets : Table() {
        val ticket_id = integer("ticket_id").autoIncrement()

        val place_number = integer("place_number")
        val time = long("time")
        val movie_title = varchar("movie_title", length = 50)
        val order_id = reference("order_id", OrderService.Orders.order_id).nullable()

        override val primaryKey = PrimaryKey(ticket_id)
    }

    init {
        transaction(database) {
            SchemaUtils.create(Tickets)
        }
    }

    private suspend fun <T> dbQuery(block: suspend () -> T): T =
        newSuspendedTransaction(Dispatchers.IO) { block() }

    suspend fun create(ticketDTO: TicketDTO): Int = dbQuery {
        Tickets.insert {
            it[place_number] = ticketDTO.placeNumber
            it[time] = ticketDTO.time
            it[movie_title] = ticketDTO.movieTitle
            it[order_id] = ticketDTO.orderId
        }[ticket_id]
    }

    suspend fun batchCreate(ticketDTOs: List<TicketDTO>) = dbQuery {
        Tickets.batchInsert(ticketDTOs) { ticketDTO ->
            this[place_number] = ticketDTO.placeNumber
            this[time] = ticketDTO.time
            this[movie_title] = ticketDTO.movieTitle
            this[order_id] = ticketDTO.orderId
        }.map { it[ticket_id] }
    }

    suspend fun readAll(): List<Ticket> = dbQuery {
        Tickets.selectAll()
            .map { it.toTicket() }
    }

    suspend fun readAllWithMovieTitleAndTimeAndOrderId(movieTitle: String, time: Long, orderId: Int?) = dbQuery {
        Tickets.select { (movie_title eq movieTitle) and (Tickets.time eq time) and (order_id eq orderId) }
            .map { it.toTicket() }
    }

    suspend fun readAllWithOrderId(orderId: Int?): List<Ticket> = dbQuery {
        Tickets.select { order_id eq orderId }
            .map { it.toTicket() }
    }

    suspend fun read(ticketId: Int): Ticket? = dbQuery {
        Tickets.select { ticket_id eq ticketId }
            .map { it.toTicket() }
            .singleOrNull()
    }

    suspend fun update(ticketId: Int, ticketDTO: TicketDTO) = dbQuery {
        Tickets.update({ ticket_id eq ticketId }) {
            it[place_number] = ticketDTO.placeNumber
            it[time] = ticketDTO.time
            it[movie_title] = ticketDTO.movieTitle
            it[order_id] = ticketDTO.orderId
        }
    }

    /**
     * Function to update the order ID of a ticket by its ticket ID.
     *
     * This function updates the order ID of a ticket in the database where the ticket ID matches
     * the specified [ticketId]. It sets the order ID to the provided value [orderId].
     *
     * @param ticketId The ticket ID of the ticket to update.
     * @param orderId The new order ID to set for the specified ticket.
     *
     * @return Unit.
     */
    suspend fun updateTicketOrderId(ticketId: Int, orderId: Int?) = dbQuery {
        Tickets.update({ ticket_id eq ticketId }) {
            it[order_id] = orderId
        }
    }

    suspend fun updateTicketOrderIds(ticketIds: List<Int>, orderId: Int) = dbQuery {
        Tickets.update({ ticket_id inList ticketIds }) {
            it[order_id] = orderId
        }
    }

    /**
     * Function to update the order IDs of tickets based on their order ID.
     *
     * This function updates the order IDs of tickets in the database where the current order ID matches
     * the specified [orderId]. It sets the order ID to the new value [newOrderId].
     *
     * @param orderId The current order ID to match tickets for updating.
     * @param newOrderId The new order ID to set for the matched tickets.
     *
     * @return Unit.
     */
    suspend fun updateTicketOrderIdsByOrderId(orderId: Int?, newOrderId: Int?) = dbQuery {
        Tickets.update({ order_id eq orderId }) {
            it[order_id] = newOrderId
        }
    }

    suspend fun delete(ticketId: Int) {
        dbQuery {
            Tickets.deleteWhere { ticket_id eq ticketId }
        }
    }

    suspend fun deleteAllWithOrderId(orderId: Int) {
        dbQuery {
            Tickets.deleteWhere { order_id eq orderId }
        }
    }

    private fun ResultRow.toTicket() = Ticket(
        ticketId = this[ticket_id],
        placeNumber = this[place_number],
        time = this[time],
        movieTitle = this[movie_title],
        orderId = this[order_id]
    )
}