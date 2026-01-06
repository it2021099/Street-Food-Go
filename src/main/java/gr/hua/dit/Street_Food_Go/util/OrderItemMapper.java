package gr.hua.dit.Street_Food_Go.util;

import gr.hua.dit.Street_Food_Go.dto.OrderItemView;
import gr.hua.dit.Street_Food_Go.model.OrderItem;

import java.util.List;
import java.util.stream.Collectors;

public class OrderItemMapper {

    private OrderItemMapper() {
    }

    public static OrderItemView toView(OrderItem orderItem) {
        if (orderItem == null) {
            return null;
        }
        return new OrderItemView(
                orderItem.getId(),
                orderItem.getOrder() != null ? orderItem.getOrder().getId() : null,
                orderItem.getMenuItem() != null ? orderItem.getMenuItem().getId() : null,
                orderItem.getMenuItem() != null ? orderItem.getMenuItem().getName() : null,
                orderItem.getQuantity(),
                orderItem.getPrice()
        );
    }

    public static List<OrderItemView> toViewList(List<OrderItem> orderItems) {
        if (orderItems == null) {
            return null;
        }
        return orderItems.stream()
                .map(OrderItemMapper::toView)
                .collect(Collectors.toList());
    }
}
