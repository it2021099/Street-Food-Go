package gr.hua.dit.Street_Food_Go.controller.web;

import gr.hua.dit.Street_Food_Go.model.*;
import gr.hua.dit.Street_Food_Go.security.CustomUserDetails;
import gr.hua.dit.Street_Food_Go.service.*;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/customer")
public class CustomerWebController {

    private final AddressService addressService;
    private final OrderService orderService;
    private final RestaurantService restaurantService;
    private final MenuItemService menuItemService;

    public CustomerWebController(final AddressService addressService,
                                  final OrderService orderService,
                                  final RestaurantService restaurantService,
                                  final MenuItemService menuItemService) {
        if (addressService == null) {
            throw new NullPointerException("addressService cannot be null");
        }
        if (orderService == null) {
            throw new NullPointerException("orderService cannot be null");
        }
        if (restaurantService == null) {
            throw new NullPointerException("restaurantService cannot be null");
        }
        if (menuItemService == null) {
            throw new NullPointerException("menuItemService cannot be null");
        }
        this.addressService = addressService;
        this.orderService = orderService;
        this.restaurantService = restaurantService;
        this.menuItemService = menuItemService;
    }

    @GetMapping("/dashboard")
    public String dashboard(@AuthenticationPrincipal CustomUserDetails userDetails,
                           @RequestParam(required = false) String search, Model model) {
        List<Restaurant> restaurants;
        if (search != null && !search.trim().isEmpty()) {
            restaurants = restaurantService.searchRestaurants(search.trim());
            model.addAttribute("searchQuery", search);
        } else {
            restaurants = restaurantService.getAllRestaurants();
        }
        model.addAttribute("restaurants", restaurants);

        // Get active orders for the current user (not completed, not cancelled, not rejected)
        List<Order> allOrders = orderService.getOrdersByCustomerId(userDetails.getId());
        List<Order> activeOrders = allOrders.stream()
                .filter(o -> o.getStatus() != OrderStatus.DELIVERED &&
                            o.getStatus() != OrderStatus.COMPLETED &&
                            o.getStatus() != OrderStatus.CANCELLED &&
                            o.getStatus() != OrderStatus.REJECTED)
                .collect(java.util.stream.Collectors.toList());
        model.addAttribute("activeOrders", activeOrders);

        return "customer/dashboard";
    }

    @GetMapping("/restaurant/{id}")
    public String restaurantDetail(@AuthenticationPrincipal CustomUserDetails userDetails,
                                   @PathVariable Long id, Model model) {
        return restaurantService.getRestaurantById(id)
                .map(restaurant -> {
                    model.addAttribute("restaurant", restaurant);
                    model.addAttribute("menuItems", menuItemService.getAvailableMenuItems(id));
                    // Pass customer's addresses for delivery option
                    List<Address> addresses = addressService.getAddressesByUserId(userDetails.getId());
                    model.addAttribute("addresses", addresses);
                    return "customer/restaurant-detail";
                })
                .orElse("redirect:/customer/dashboard");
    }

    @PostMapping("/checkout")
    public String checkout(@AuthenticationPrincipal CustomUserDetails userDetails,
                          @RequestParam Long restaurantId,
                          @RequestParam String cartData,
                          @RequestParam(required = false, defaultValue = "PICKUP") String orderType,
                          @RequestParam(required = false) Long addressId,
                          RedirectAttributes redirectAttributes) {
        try {
            // Check if restaurant exists and is open
            Restaurant restaurant = restaurantService.getRestaurantById(restaurantId).orElse(null);
            if (restaurant == null) {
                redirectAttributes.addFlashAttribute("error", "Restaurant not found");
                return "redirect:/customer/dashboard";
            }
            if (!restaurant.isOpen()) {
                redirectAttributes.addFlashAttribute("error", "This restaurant is currently closed");
                return "redirect:/customer/restaurant/" + restaurantId;
            }

            // Parse order type
            OrderType type = OrderType.valueOf(orderType);

            // Validate delivery address for delivery orders
            if (type == OrderType.DELIVERY && addressId == null) {
                redirectAttributes.addFlashAttribute("error", "Please select a delivery address");
                return "redirect:/customer/restaurant/" + restaurantId;
            }

            // Parse cart data from JSON
            com.fasterxml.jackson.databind.ObjectMapper mapper = new com.fasterxml.jackson.databind.ObjectMapper();
            List<Map<String, Object>> cartItems = mapper.readValue(cartData,
                    new com.fasterxml.jackson.core.type.TypeReference<List<Map<String, Object>>>() {});

            if (cartItems.isEmpty()) {
                redirectAttributes.addFlashAttribute("error", "Cart is empty");
                return "redirect:/customer/restaurant/" + restaurantId;
            }

            // Calculate total and validate minimum order
            BigDecimal total = BigDecimal.ZERO;
            for (Map<String, Object> item : cartItems) {
                Long menuItemId = Long.parseLong(item.get("id").toString());
                int quantity = ((Number) item.get("quantity")).intValue();
                MenuItem menuItem = menuItemService.getMenuItemById(menuItemId).orElse(null);
                if (menuItem != null) {
                    total = total.add(menuItem.getPrice().multiply(BigDecimal.valueOf(quantity)));
                }
            }

            // Check minimum order value
            if (restaurant.getMinimumOrderValue() != null && total.compareTo(restaurant.getMinimumOrderValue()) < 0) {
                redirectAttributes.addFlashAttribute("error",
                    "Minimum order is " + restaurant.getMinimumOrderValue() + "€. Your cart total is " + total + "€");
                return "redirect:/customer/restaurant/" + restaurantId;
            }

            // Create order with delivery address (ETA calculated automatically via OSRM for delivery orders)
            Order order = orderService.createOrder(
                    userDetails.getId(),
                    restaurantId,
                    type,
                    type == OrderType.DELIVERY ? addressId : null
            );

            // Add items to order
            for (Map<String, Object> item : cartItems) {
                Long menuItemId = Long.parseLong(item.get("id").toString());
                int quantity = ((Number) item.get("quantity")).intValue();
                orderService.addItemToOrder(order.getId(), menuItemId, quantity);
            }

            redirectAttributes.addFlashAttribute("orderId", order.getId());
            return "redirect:/customer/order-success";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Failed to create order: " + e.getMessage());
            return "redirect:/customer/restaurant/" + restaurantId;
        }
    }

    @GetMapping("/order-success")
    public String orderSuccess(@ModelAttribute("orderId") Long orderId, Model model) {
        model.addAttribute("orderId", orderId);
        return "customer/order-success";
    }

    // ========== Addresses ==========

    @GetMapping("/addresses")
    public String addresses(@AuthenticationPrincipal CustomUserDetails userDetails, Model model) {
        List<Address> addresses = addressService.getAddressesByUserId(userDetails.getId());
        model.addAttribute("addresses", addresses);
        return "customer/addresses";
    }

    @PostMapping("/addresses")
    public String createAddress(@AuthenticationPrincipal CustomUserDetails userDetails,
                                 @RequestParam String streetAddress,
                                 @RequestParam String city,
                                 @RequestParam String postalCode,
                                 @RequestParam(required = false) Double latitude,
                                 @RequestParam(required = false) Double longitude,
                                 RedirectAttributes redirectAttributes) {
        Address address = new Address();
        address.setStreetAddress(streetAddress);
        address.setCity(city);
        address.setPostalCode(postalCode);
        address.setLatitude(latitude);
        address.setLongitude(longitude);

        addressService.createAddress(userDetails.getId(), address);
        redirectAttributes.addFlashAttribute("success", "Η διεύθυνση προστέθηκε");
        return "redirect:/customer/addresses";
    }

    @PostMapping("/addresses/{id}/default")
    public String setDefaultAddress(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        addressService.setAsDefault(id);
        redirectAttributes.addFlashAttribute("success", "Η διεύθυνση ορίστηκε ως προεπιλογή");
        return "redirect:/customer/addresses";
    }

    @PostMapping("/addresses/{id}/delete")
    public String deleteAddress(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        addressService.deleteAddress(id);
        redirectAttributes.addFlashAttribute("success", "Η διεύθυνση διαγράφηκε");
        return "redirect:/customer/addresses";
    }

    // ========== Orders ==========

    @GetMapping("/orders")
    public String orders(@AuthenticationPrincipal CustomUserDetails userDetails, Model model) {
        List<Order> orders = orderService.getOrdersByCustomerId(userDetails.getId());
        model.addAttribute("orders", orders);
        return "customer/orders";
    }

    @GetMapping("/orders/{id}")
    public String orderDetail(@AuthenticationPrincipal CustomUserDetails userDetails,
                               @PathVariable Long id, Model model) {
        return orderService.getOrderById(id)
                .filter(order -> order.getCustomer().getId().equals(userDetails.getId()))
                .map(order -> {
                    model.addAttribute("order", order);
                    return "customer/order-detail";
                })
                .orElse("redirect:/customer/orders");
    }

    @GetMapping("/order/new")
    public String newOrderForm(@AuthenticationPrincipal CustomUserDetails userDetails,
                                @RequestParam Long restaurantId, Model model) {
        return restaurantService.getRestaurantById(restaurantId)
                .map(restaurant -> {
                    if (!restaurant.isOpen()) {
                        return "redirect:/restaurant/" + restaurantId;
                    }
                    model.addAttribute("restaurant", restaurant);
                    model.addAttribute("menuItems", menuItemService.getAvailableMenuItems(restaurantId));
                    model.addAttribute("addresses", addressService.getAddressesByUserId(userDetails.getId()));
                    return "customer/order-create";
                })
                .orElse("redirect:/");
    }

    @PostMapping("/order/create")
    public String createOrder(@AuthenticationPrincipal CustomUserDetails userDetails,
                               @RequestParam Long restaurantId,
                               @RequestParam String orderType,
                               @RequestParam(required = false) Long deliveryAddressId,
                               @RequestParam(required = false) String specialInstructions,
                               @RequestParam Map<String, String> allParams,
                               RedirectAttributes redirectAttributes) {

        // Check if restaurant is open
        Restaurant restaurant = restaurantService.getRestaurantById(restaurantId).orElse(null);
        if (restaurant == null || !restaurant.isOpen()) {
            redirectAttributes.addFlashAttribute("error", "Το κατάστημα είναι κλειστό");
            return "redirect:/";
        }

        // Parse order type
        OrderType type = OrderType.valueOf(orderType);

        // For delivery, address is required
        if (type == OrderType.DELIVERY && deliveryAddressId == null) {
            redirectAttributes.addFlashAttribute("error", "Επιλέξτε διεύθυνση παράδοσης");
            return "redirect:/customer/order/new?restaurantId=" + restaurantId;
        }

        // Create order
        Order order = orderService.createOrder(
                userDetails.getId(),
                restaurantId,
                type,
                type == OrderType.DELIVERY ? deliveryAddressId : null
        );

        // Add items to order
        BigDecimal total = BigDecimal.ZERO;
        for (Map.Entry<String, String> entry : allParams.entrySet()) {
            if (entry.getKey().startsWith("items[") && entry.getKey().endsWith("]")) {
                int quantity = Integer.parseInt(entry.getValue());
                if (quantity > 0) {
                    String itemIdStr = entry.getKey().substring(6, entry.getKey().length() - 1);
                    Long menuItemId = Long.parseLong(itemIdStr);
                    orderService.addItemToOrder(order.getId(), menuItemId, quantity);

                    MenuItem item = menuItemService.getMenuItemById(menuItemId).orElse(null);
                    if (item != null) {
                        total = total.add(item.getPrice().multiply(BigDecimal.valueOf(quantity)));
                    }
                }
            }
        }

        // Check minimum order value
        if (restaurant.getMinimumOrderValue() != null && total.compareTo(restaurant.getMinimumOrderValue()) < 0) {
            orderService.cancelOrder(order.getId());
            redirectAttributes.addFlashAttribute("error",
                    "Η ελάχιστη παραγγελία είναι " + restaurant.getMinimumOrderValue() + "€");
            return "redirect:/customer/order/new?restaurantId=" + restaurantId;
        }

        redirectAttributes.addFlashAttribute("success", "Η παραγγελία καταχωρήθηκε!");
        return "redirect:/customer/orders/" + order.getId();
    }

    @PostMapping("/orders/{id}/cancel")
    public String cancelOrder(@AuthenticationPrincipal CustomUserDetails userDetails,
                               @PathVariable Long id, RedirectAttributes redirectAttributes) {
        return orderService.getOrderById(id)
                .filter(order -> order.getCustomer().getId().equals(userDetails.getId()))
                .filter(order -> order.getStatus() == OrderStatus.PENDING)
                .map(order -> {
                    orderService.cancelOrder(id);
                    redirectAttributes.addFlashAttribute("success", "Η παραγγελία ακυρώθηκε");
                    return "redirect:/customer/orders";
                })
                .orElseGet(() -> {
                    redirectAttributes.addFlashAttribute("error", "Δεν μπορεί να ακυρωθεί η παραγγελία");
                    return "redirect:/customer/orders";
                });
    }
}
