package gr.hua.dit.Street_Food_Go.util;

import gr.hua.dit.Street_Food_Go.dto.OrderView;
import gr.hua.dit.Street_Food_Go.model.Order;

import java.util.List;
import java.util.stream.Collectors;

public class OrderMapper {

    private OrderMapper() {
    }

    public static OrderView toView(Order order) {
        if (order == null) {
            return null;
        }
        OrderView view = new OrderView();
        view.setId(order.getId());
        view.setOrderType(order.getOrderType());
        view.setStatus(order.getStatus());
        view.setTotalAmount(order.getTotalAmount());
        view.setEstimatedDeliveryTime(order.getEstimatedDeliveryTime());
        view.setCreatedAt(order.getCreatedAt());
        view.setUpdatedAt(order.getUpdatedAt());

        if (order.getCustomer() != null) {
            view.setCustomerId(order.getCustomer().getId());
            view.setCustomerUsername(order.getCustomer().getUsername());
        }

        if (order.getRestaurant() != null) {
            view.setRestaurantId(order.getRestaurant().getId());
            view.setRestaurantName(order.getRestaurant().getName());
        }

        if (order.getDeliveryAddress() != null) {
            view.setDeliveryAddress(AddressMapper.toView(order.getDeliveryAddress()));
        }

        if (order.getOrderItems() != null) {
            view.setOrderItems(OrderItemMapper.toViewList(order.getOrderItems()));
        }

        return view;
    }

    public static List<OrderView> toViewList(List<Order> orders) {
        if (orders == null) {
            return null;
        }
        return orders.stream()
                .map(OrderMapper::toView)
                .collect(Collectors.toList());
    }
}
