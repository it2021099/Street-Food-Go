package gr.hua.dit.Street_Food_Go.controller;

import gr.hua.dit.Street_Food_Go.dto.OrderView;
import gr.hua.dit.Street_Food_Go.model.Order;
import gr.hua.dit.Street_Food_Go.model.OrderStatus;
import gr.hua.dit.Street_Food_Go.model.OrderType;
import gr.hua.dit.Street_Food_Go.service.OrderService;
import gr.hua.dit.Street_Food_Go.util.OrderMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/orders")
@Tag(name = "Orders", description = "Order management endpoints")
public class OrderController {

    private final OrderService orderService;

    public OrderController(final OrderService orderService) {
        if (orderService == null) {
            throw new NullPointerException("orderService cannot be null");
        }
        this.orderService = orderService;
    }

    @PostMapping
    @PreAuthorize("hasRole('CUSTOMER')")
    @Operation(summary = "Create a new order", description = "Creates a new order for a customer")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Order created successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid request"),
            @ApiResponse(responseCode = "403", description = "Access denied")
    })
    public ResponseEntity<OrderView> createOrder(
            @Parameter(description = "Customer ID") @RequestParam Long customerId,
            @Parameter(description = "Restaurant ID") @RequestParam Long restaurantId,
            @Parameter(description = "Order type (DELIVERY or PICKUP)") @RequestParam OrderType orderType,
            @Parameter(description = "Delivery address ID (required for DELIVERY)") @RequestParam(required = false) Long deliveryAddressId) {
        try {
            Order createdOrder = orderService.createOrder(customerId, restaurantId, orderType, deliveryAddressId);
            return new ResponseEntity<>(OrderMapper.toView(createdOrder), HttpStatus.CREATED);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @PostMapping("/{orderId}/items")
    @PreAuthorize("hasRole('CUSTOMER')")
    @Operation(summary = "Add item to order", description = "Adds a menu item to an existing order")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Item added successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid request"),
            @ApiResponse(responseCode = "404", description = "Order or menu item not found")
    })
    public ResponseEntity<OrderView> addItemToOrder(
            @Parameter(description = "Order ID") @PathVariable Long orderId,
            @Parameter(description = "Menu item ID") @RequestParam Long menuItemId,
            @Parameter(description = "Quantity") @RequestParam int quantity) {
        try {
            Order order = orderService.addItemToOrder(orderId, menuItemId, quantity);
            return ResponseEntity.ok(OrderMapper.toView(order));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get order by ID", description = "Returns an order by its ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Order found"),
            @ApiResponse(responseCode = "404", description = "Order not found")
    })
    public ResponseEntity<OrderView> getOrderById(
            @Parameter(description = "Order ID") @PathVariable Long id) {
        return orderService.getOrderById(id)
                .map(order -> ResponseEntity.ok(OrderMapper.toView(order)))
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/customer/{customerId}")
    @Operation(summary = "Get orders by customer", description = "Returns all orders for a customer")
    @ApiResponse(responseCode = "200", description = "List of orders")
    public ResponseEntity<List<OrderView>> getOrdersByCustomerId(
            @Parameter(description = "Customer ID") @PathVariable Long customerId) {
        List<Order> orders = orderService.getOrdersByCustomerId(customerId);
        return ResponseEntity.ok(OrderMapper.toViewList(orders));
    }

    @GetMapping("/restaurant/{restaurantId}")
    @PreAuthorize("hasRole('RESTAURANT_OWNER')")
    @Operation(summary = "Get orders by restaurant", description = "Returns all orders for a restaurant")
    @ApiResponse(responseCode = "200", description = "List of orders")
    public ResponseEntity<List<OrderView>> getOrdersByRestaurantId(
            @Parameter(description = "Restaurant ID") @PathVariable Long restaurantId) {
        List<Order> orders = orderService.getOrdersByRestaurantId(restaurantId);
        return ResponseEntity.ok(OrderMapper.toViewList(orders));
    }

    @GetMapping("/status/{status}")
    @PreAuthorize("hasRole('RESTAURANT_OWNER')")
    @Operation(summary = "Get orders by status", description = "Returns all orders with a specific status")
    @ApiResponse(responseCode = "200", description = "List of orders")
    public ResponseEntity<List<OrderView>> getOrdersByStatus(
            @Parameter(description = "Order status") @PathVariable OrderStatus status) {
        List<Order> orders = orderService.getOrdersByStatus(status);
        return ResponseEntity.ok(OrderMapper.toViewList(orders));
    }

    @PatchMapping("/{id}/status/{status}")
    @PreAuthorize("hasRole('RESTAURANT_OWNER')")
    @Operation(summary = "Update order status", description = "Updates the status of an order")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Status updated"),
            @ApiResponse(responseCode = "400", description = "Invalid status transition"),
            @ApiResponse(responseCode = "404", description = "Order not found")
    })
    public ResponseEntity<OrderView> updateOrderStatus(
            @Parameter(description = "Order ID") @PathVariable Long id,
            @Parameter(description = "New status") @PathVariable OrderStatus status) {
        try {
            Order order = orderService.updateOrderStatus(id, status);
            return ResponseEntity.ok(OrderMapper.toView(order));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @PatchMapping("/{id}/cancel")
    @Operation(summary = "Cancel order", description = "Cancels an order (only if not already delivered)")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Order cancelled"),
            @ApiResponse(responseCode = "400", description = "Cannot cancel order in current status")
    })
    public ResponseEntity<OrderView> cancelOrder(
            @Parameter(description = "Order ID") @PathVariable Long id) {
        try {
            Order order = orderService.cancelOrder(id);
            return ResponseEntity.ok(OrderMapper.toView(order));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().build();
        }
    }
}
