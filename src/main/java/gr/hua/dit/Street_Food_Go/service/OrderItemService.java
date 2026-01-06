package gr.hua.dit.Street_Food_Go.service;

import gr.hua.dit.Street_Food_Go.model.OrderItem;

import java.util.List;
import java.util.Optional;

/**
 * Service interface for managing {@code OrderItem} entities.
 */
public interface OrderItemService {

    List<OrderItem> getOrderItemsByOrderId(Long orderId);

    Optional<OrderItem> getOrderItemById(Long id);

    void deleteOrderItem(Long id);
}
