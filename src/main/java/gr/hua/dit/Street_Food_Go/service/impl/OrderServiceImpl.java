package gr.hua.dit.Street_Food_Go.service.impl;

import gr.hua.dit.Street_Food_Go.model.*;
import gr.hua.dit.Street_Food_Go.repository.*;
import gr.hua.dit.Street_Food_Go.service.OrderService;
import gr.hua.dit.Street_Food_Go.service.external.DeliveryTimeResult;
import gr.hua.dit.Street_Food_Go.service.external.DeliveryTimeService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class OrderServiceImpl implements OrderService {

    private static final Logger logger = LoggerFactory.getLogger(OrderServiceImpl.class);

    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;
    private final UserRepository userRepository;
    private final RestaurantRepository restaurantRepository;
    private final MenuItemRepository menuItemRepository;
    private final AddressRepository addressRepository;
    private final DeliveryTimeService deliveryTimeService;

    public OrderServiceImpl(
            final OrderRepository orderRepository,
            final OrderItemRepository orderItemRepository,
            final UserRepository userRepository,
            final RestaurantRepository restaurantRepository,
            final MenuItemRepository menuItemRepository,
            final AddressRepository addressRepository,
            final DeliveryTimeService deliveryTimeService) {
        if (orderRepository == null) {
            throw new NullPointerException("orderRepository cannot be null");
        }
        if (orderItemRepository == null) {
            throw new NullPointerException("orderItemRepository cannot be null");
        }
        if (userRepository == null) {
            throw new NullPointerException("userRepository cannot be null");
        }
        if (restaurantRepository == null) {
            throw new NullPointerException("restaurantRepository cannot be null");
        }
        if (menuItemRepository == null) {
            throw new NullPointerException("menuItemRepository cannot be null");
        }
        if (addressRepository == null) {
            throw new NullPointerException("addressRepository cannot be null");
        }
        if (deliveryTimeService == null) {
            throw new NullPointerException("deliveryTimeService cannot be null");
        }
        this.orderRepository = orderRepository;
        this.orderItemRepository = orderItemRepository;
        this.userRepository = userRepository;
        this.restaurantRepository = restaurantRepository;
        this.menuItemRepository = menuItemRepository;
        this.addressRepository = addressRepository;
        this.deliveryTimeService = deliveryTimeService;
    }

    @Override
    public Order createOrder(Long customerId, Long restaurantId, OrderType orderType, Long deliveryAddressId) {
        User customer = userRepository.findById(customerId)
                .orElseThrow(() -> new RuntimeException("Customer not found with id: " + customerId));

        Restaurant restaurant = restaurantRepository.findById(restaurantId)
                .orElseThrow(() -> new RuntimeException("Restaurant not found with id: " + restaurantId));

        Order order = new Order();
        order.setCustomer(customer);
        order.setRestaurant(restaurant);
        order.setOrderType(orderType);
        order.setStatus(OrderStatus.PENDING);
        order.setTotalAmount(BigDecimal.ZERO);

        if (orderType == OrderType.DELIVERY && deliveryAddressId != null) {
            Address deliveryAddress = addressRepository.findById(deliveryAddressId)
                    .orElseThrow(() -> new RuntimeException("Address not found with id: " + deliveryAddressId));
            order.setDeliveryAddress(deliveryAddress);

            // Calculate estimated delivery time using external OSRM service
            calculateAndSetDeliveryTime(order, restaurant, deliveryAddress);
        }

        return orderRepository.save(order);
    }

    /**
     * Calculates the estimated delivery time using the external OSRM routing service.
     * This demonstrates the consumption of an external (black-box) service via REST client.
     */
    private void calculateAndSetDeliveryTime(Order order, Restaurant restaurant, Address deliveryAddress) {
        try {
            DeliveryTimeResult result = deliveryTimeService.calculateDeliveryTime(
                    restaurant.getLatitude(),
                    restaurant.getLongitude(),
                    deliveryAddress.getLatitude(),
                    deliveryAddress.getLongitude()
            );

            if (result.isSuccess()) {
                LocalDateTime estimatedDelivery = LocalDateTime.now().plusMinutes(result.getDurationMinutes());
                order.setEstimatedDeliveryTime(estimatedDelivery);
                logger.info("Estimated delivery time set to: {} ({} minutes, {} km)",
                        estimatedDelivery, result.getDurationMinutes(), result.getDistanceKm());
            } else {
                logger.warn("Could not calculate delivery time: {}", result.getErrorMessage());
                // Set a default estimate if external service fails (30 minutes)
                order.setEstimatedDeliveryTime(LocalDateTime.now().plusMinutes(30));
            }
        } catch (Exception e) {
            logger.error("Error calculating delivery time", e);
            // Set a default estimate if external service fails
            order.setEstimatedDeliveryTime(LocalDateTime.now().plusMinutes(30));
        }
    }

    @Override
    public Order addItemToOrder(Long orderId, Long menuItemId, int quantity) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found with id: " + orderId));

        MenuItem menuItem = menuItemRepository.findById(menuItemId)
                .orElseThrow(() -> new RuntimeException("MenuItem not found with id: " + menuItemId));

        OrderItem orderItem = new OrderItem();
        orderItem.setOrder(order);
        orderItem.setMenuItem(menuItem);
        orderItem.setQuantity(quantity);
        orderItem.setPrice(menuItem.getPrice().multiply(BigDecimal.valueOf(quantity)));

        orderItemRepository.save(orderItem);

        // Update total amount
        BigDecimal newTotal = order.getTotalAmount().add(orderItem.getPrice());
        order.setTotalAmount(newTotal);

        return orderRepository.save(order);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Order> getOrdersByCustomerId(Long customerId) {
        return orderRepository.findByCustomerId(customerId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Order> getOrdersByRestaurantId(Long restaurantId) {
        return orderRepository.findByRestaurantId(restaurantId);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Order> getOrderById(Long id) {
        return orderRepository.findById(id);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Order> getOrdersByStatus(OrderStatus status) {
        return orderRepository.findByStatus(status);
    }

    @Override
    public Order updateOrderStatus(Long orderId, OrderStatus newStatus) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found with id: " + orderId));
        order.setStatus(newStatus);
        return orderRepository.save(order);
    }

    @Override
    public Order cancelOrder(Long orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found with id: " + orderId));

        if (order.getStatus() == OrderStatus.COMPLETED || order.getStatus() == OrderStatus.OUT_FOR_DELIVERY) {
            throw new RuntimeException("Cannot cancel order in status: " + order.getStatus());
        }

        order.setStatus(OrderStatus.CANCELLED);
        return orderRepository.save(order);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Order> getOrdersInDateRange(LocalDateTime start, LocalDateTime end) {
        return orderRepository.findByCreatedAtBetween(start, end);
    }
}
