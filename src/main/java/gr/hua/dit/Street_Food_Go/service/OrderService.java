package gr.hua.dit.Street_Food_Go.service;

import gr.hua.dit.Street_Food_Go.model.Order;
import gr.hua.dit.Street_Food_Go.model.OrderStatus;
import gr.hua.dit.Street_Food_Go.model.OrderType;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Service interface for managing {@code Order} entities.
 */
public interface OrderService {

    Order createOrder(Long customerId, Long restaurantId, OrderType orderType, Long deliveryAddressId);

    Order addItemToOrder(Long orderId, Long menuItemId, int quantity);

    List<Order> getOrdersByCustomerId(Long customerId);

    List<Order> getOrdersByRestaurantId(Long restaurantId);

    Optional<Order> getOrderById(Long id);

    List<Order> getOrdersByStatus(OrderStatus status);

    Order updateOrderStatus(Long orderId, OrderStatus newStatus);

    Order cancelOrder(Long orderId);

    List<Order> getOrdersInDateRange(LocalDateTime start, LocalDateTime end);
}
