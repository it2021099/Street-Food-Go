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
    public String dashboard() {
        return "customer/dashboard";
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
