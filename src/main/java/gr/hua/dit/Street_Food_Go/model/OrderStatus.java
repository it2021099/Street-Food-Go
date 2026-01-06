package gr.hua.dit.Street_Food_Go.model;

public enum OrderStatus {
    PENDING,           // Order placed, waiting for restaurant response
    ACCEPTED,          // Restaurant accepted the order
    REJECTED,          // Restaurant rejected the order
    PREPARING,         // Restaurant is preparing the order
    READY_FOR_PICKUP,  // Food is ready for pickup
    OUT_FOR_DELIVERY,  // Delivery order is out for delivery
    DELIVERED,         // Order delivered (for delivery orders)
    COMPLETED,         // Order completed (for pickup orders)
    CANCELLED          // Customer cancelled the order
}
