package kpi.plugins

import kotlinx.serialization.Serializable
import org.jetbrains.exposed.sql.Database

//CREATE TABLE Orders(
//    order_id INT PRIMARY KEY,
//    ticket_id INT,
//    CONSTRAINT order_id_pk PRIMARY KEY (order_id),
//    CONSTRAINT ticket_id_fk FOREIGN KEY (ticket_id) REFERENCES Tickets(ticket_id)
//);

@Serializable
data class Order(
    val orderId: Int,
    val ticketId: Int
)

//@Serializable
//data class OrderDTO(
//    val
//)


class OrderService(private val database: Database) {
}