package kpi.plugins

import kotlinx.coroutines.Dispatchers
import kotlinx.serialization.Serializable
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction
import org.jetbrains.exposed.sql.transactions.transaction

@Serializable
data class Order(
    val orderId: Int,
    val viewer: Viewer,
    val tickets: List<Ticket>
)

@Serializable
data class OrderDTO(
    val viewerId: Int,
    val ticketIds: List<Int>
)

class OrderService(
    database: Database,
    private val viewerService: ViewerService,
    private val ticketService: TicketService
) {
    object Orders : Table() {
        val order_id = integer("order_id").autoIncrement()

        val viewer_id = reference("viewer_id", ViewerService.Viewers.viewer_id)

        override val primaryKey = PrimaryKey(order_id)
    }

    init {
        transaction(database) {
            SchemaUtils.create(Orders)
        }
    }

    private suspend fun <T> dbQuery(block: suspend () -> T): T =
        newSuspendedTransaction(Dispatchers.IO) { block() }

    suspend fun create(orderDTO: OrderDTO): Int {
        val newOrderId = dbQuery {
            Orders.insert {
                it[viewer_id] = orderDTO.viewerId
            }[Orders.order_id]
        }

        dbQuery {
            ticketService.updateTicketOrderIds(ticketIds = orderDTO.ticketIds, orderId = newOrderId)
        }

        return newOrderId
    }

    suspend fun readAll(): List<Order> = dbQuery {
        Orders.selectAll()
            .map { it.toOrder() }
    }

    suspend fun readAllWithViewerId(viewerId: Int): List<Order> = dbQuery {
        Orders.select { Orders.viewer_id eq viewerId }
            .map { it.toOrder() }
    }

    suspend fun read(orderId: Int): Order? = dbQuery {
        Orders.select { Orders.order_id eq orderId }
            .map { it.toOrder() }
            .singleOrNull()
    }

    suspend fun update(orderId: Int, orderDTO: OrderDTO) = dbQuery {
        Orders.update({ Orders.order_id eq orderId }) {
            it[viewer_id] = orderDTO.viewerId
        }
    }

    suspend fun delete(orderId: Int) {
        dbQuery {
            ticketService.deleteAllWithOrderId(orderId)
        }

        dbQuery {
            Orders.deleteWhere { order_id eq orderId }
        }
    }

    private suspend fun ResultRow.toOrder() = Order(
        orderId = this[Orders.order_id],
        viewer = viewerService.read(this[Orders.viewer_id])!!,
        tickets = ticketService.readAllWithOrderId(this[Orders.order_id])
    )
}