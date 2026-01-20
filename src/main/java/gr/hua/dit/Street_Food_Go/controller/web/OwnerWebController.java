package gr.hua.dit.Street_Food_Go.controller.web;

import gr.hua.dit.Street_Food_Go.model.*;
import gr.hua.dit.Street_Food_Go.security.CustomUserDetails;
import gr.hua.dit.Street_Food_Go.service.MenuItemService;
import gr.hua.dit.Street_Food_Go.service.OrderService;
import gr.hua.dit.Street_Food_Go.service.RestaurantService;
import gr.hua.dit.Street_Food_Go.service.external.DeliveryTimeService;
import gr.hua.dit.Street_Food_Go.service.external.DeliveryTimeResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/owner")
public class OwnerWebController {

    private static final Logger logger = LoggerFactory.getLogger(OwnerWebController.class);

    private final RestaurantService restaurantService;
    private final MenuItemService menuItemService;
    private final OrderService orderService;
    private final DeliveryTimeService deliveryTimeService;

    public OwnerWebController(final RestaurantService restaurantService,
                               final MenuItemService menuItemService,
                               final OrderService orderService,
                               final DeliveryTimeService deliveryTimeService) {
        if (restaurantService == null) {
            throw new NullPointerException("restaurantService cannot be null");
        }
        if (menuItemService == null) {
            throw new NullPointerException("menuItemService cannot be null");
        }
        if (orderService == null) {
            throw new NullPointerException("orderService cannot be null");
        }
        if (deliveryTimeService == null) {
            throw new NullPointerException("deliveryTimeService cannot be null");
        }
        this.restaurantService = restaurantService;
        this.menuItemService = menuItemService;
        this.orderService = orderService;
        this.deliveryTimeService = deliveryTimeService;
    }

    @GetMapping("/dashboard")
    public String dashboard(@AuthenticationPrincipal CustomUserDetails userDetails, Model model) {
        List<Restaurant> restaurants = restaurantService.getRestaurantsByOwnerId(userDetails.getId());

        // If owner has no restaurant, redirect to create one
        if (restaurants.isEmpty()) {
            return "redirect:/owner/restaurants/new";
        }

        model.addAttribute("restaurantsCount", restaurants.size());
        // Pass the first restaurant for the open/close toggle (simplified - owner has one restaurant)
        model.addAttribute("restaurant", restaurants.get(0));

        // Get pending and active orders across all restaurants
        List<Order> pendingOrders = new ArrayList<>();
        List<Order> activeOrders = new ArrayList<>();

        for (Restaurant restaurant : restaurants) {
            List<Order> orders = orderService.getOrdersByRestaurantId(restaurant.getId());
            pendingOrders.addAll(orders.stream()
                    .filter(o -> o.getStatus() == OrderStatus.PENDING)
                    .collect(Collectors.toList()));
            activeOrders.addAll(orders.stream()
                    .filter(o -> o.getStatus() == OrderStatus.ACCEPTED ||
                                 o.getStatus() == OrderStatus.PREPARING ||
                                 o.getStatus() == OrderStatus.READY_FOR_PICKUP ||
                                 o.getStatus() == OrderStatus.OUT_FOR_DELIVERY)
                    .collect(Collectors.toList()));
        }

        model.addAttribute("pendingOrders", pendingOrders);
        model.addAttribute("activeOrders", activeOrders);

        return "owner/dashboard";
    }

    @GetMapping("/menu")
    public String menu(@AuthenticationPrincipal CustomUserDetails userDetails, Model model) {
        List<Restaurant> restaurants = restaurantService.getRestaurantsByOwnerId(userDetails.getId());

        if (restaurants.isEmpty()) {
            // No restaurant yet, redirect to create one
            return "redirect:/owner/restaurants/new";
        }

        // Use the first restaurant (simplified - owner has one restaurant)
        Restaurant restaurant = restaurants.get(0);
        model.addAttribute("restaurant", restaurant);
        model.addAttribute("menuItems", menuItemService.getMenuItemsByRestaurantId(restaurant.getId()));

        return "owner/menu";
    }

    @PostMapping("/menu/save")
    public String saveMenu(@AuthenticationPrincipal CustomUserDetails userDetails,
                          @RequestParam Long restaurantId,
                          @RequestParam(required = false) List<Long> deleteIds,
                          @RequestParam Map<String, String> allParams,
                          RedirectAttributes redirectAttributes) {
        // Verify ownership
        Restaurant restaurant = restaurantService.getRestaurantById(restaurantId)
                .filter(r -> r.getOwner().getId().equals(userDetails.getId()))
                .orElse(null);

        if (restaurant == null) {
            redirectAttributes.addFlashAttribute("error", "Restaurant not found");
            return "redirect:/owner/menu";
        }

        // Delete items marked for deletion
        if (deleteIds != null) {
            for (Long deleteId : deleteIds) {
                menuItemService.deleteMenuItem(deleteId);
            }
        }

        // Process items from form
        int index = 0;
        while (allParams.containsKey("items[" + index + "].name")) {
            String idStr = allParams.get("items[" + index + "].id");
            String name = allParams.get("items[" + index + "].name");
            String description = allParams.get("items[" + index + "].description");
            String priceStr = allParams.get("items[" + index + "].price");

            if (name != null && !name.trim().isEmpty() && priceStr != null) {
                BigDecimal price = new BigDecimal(priceStr);

                if (idStr != null && !idStr.isEmpty()) {
                    // Update existing item
                    Long itemId = Long.parseLong(idStr);
                    menuItemService.getMenuItemById(itemId).ifPresent(item -> {
                        item.setName(name);
                        item.setDescription(description);
                        item.setPrice(price);
                        menuItemService.updateMenuItem(itemId, item);
                    });
                } else {
                    // Create new item
                    MenuItem newItem = new MenuItem();
                    newItem.setName(name);
                    newItem.setDescription(description);
                    newItem.setPrice(price);
                    newItem.setAvailable(true);
                    menuItemService.createMenuItem(restaurantId, newItem);
                }
            }
            index++;
        }

        redirectAttributes.addFlashAttribute("success", "Menu saved successfully");
        return "redirect:/owner/menu";
    }

    // ========== Restaurants ==========

    @GetMapping("/restaurants")
    public String restaurants(@AuthenticationPrincipal CustomUserDetails userDetails, Model model) {
        List<Restaurant> restaurants = restaurantService.getRestaurantsByOwnerId(userDetails.getId());
        model.addAttribute("restaurants", restaurants);
        return "owner/restaurants";
    }

    @GetMapping("/restaurants/new")
    public String newRestaurantForm(Model model) {
        model.addAttribute("restaurant", null);
        return "owner/restaurant-form";
    }

    @PostMapping("/restaurants")
    public String createRestaurant(@AuthenticationPrincipal CustomUserDetails userDetails,
                                    @RequestParam String name,
                                    @RequestParam String cuisineType,
                                    @RequestParam String address,
                                    @RequestParam(required = false) Double latitude,
                                    @RequestParam(required = false) Double longitude,
                                    @RequestParam(required = false) BigDecimal minimumOrderValue,
                                    RedirectAttributes redirectAttributes) {
        Restaurant restaurant = new Restaurant();
        restaurant.setName(name);
        restaurant.setCuisineType(cuisineType);
        restaurant.setAddress(address);
        restaurant.setLatitude(latitude);
        restaurant.setLongitude(longitude);
        restaurant.setMinimumOrderValue(minimumOrderValue);
        restaurant.setOpen(false); // New restaurants start as closed

        restaurantService.createRestaurant(userDetails.getId(), restaurant);
        redirectAttributes.addFlashAttribute("success", "Το κατάστημα δημιουργήθηκε");
        return "redirect:/owner/dashboard";
    }

    @GetMapping("/restaurants/{id}/edit")
    public String editRestaurantForm(@AuthenticationPrincipal CustomUserDetails userDetails,
                                      @PathVariable Long id, Model model) {
        return restaurantService.getRestaurantById(id)
                .filter(r -> r.getOwner().getId().equals(userDetails.getId()))
                .map(restaurant -> {
                    model.addAttribute("restaurant", restaurant);
                    return "owner/restaurant-form";
                })
                .orElse("redirect:/owner/restaurants");
    }

    @PostMapping("/restaurants/{id}")
    public String updateRestaurant(@AuthenticationPrincipal CustomUserDetails userDetails,
                                    @PathVariable Long id,
                                    @RequestParam String name,
                                    @RequestParam String cuisineType,
                                    @RequestParam String address,
                                    @RequestParam(required = false) Double latitude,
                                    @RequestParam(required = false) Double longitude,
                                    @RequestParam(required = false) BigDecimal minimumOrderValue,
                                    RedirectAttributes redirectAttributes) {
        return restaurantService.getRestaurantById(id)
                .filter(r -> r.getOwner().getId().equals(userDetails.getId()))
                .map(existing -> {
                    existing.setName(name);
                    existing.setCuisineType(cuisineType);
                    existing.setAddress(address);
                    existing.setLatitude(latitude);
                    existing.setLongitude(longitude);
                    existing.setMinimumOrderValue(minimumOrderValue);
                    // Note: open/closed status is managed via the toggle endpoint
                    restaurantService.updateRestaurant(id, existing);
                    redirectAttributes.addFlashAttribute("success", "Το κατάστημα ενημερώθηκε");
                    return "redirect:/owner/restaurants";
                })
                .orElse("redirect:/owner/restaurants");
    }

    @PostMapping("/restaurants/{id}/toggle")
    public String toggleRestaurant(@AuthenticationPrincipal CustomUserDetails userDetails,
                                    @PathVariable Long id, RedirectAttributes redirectAttributes) {
        return restaurantService.getRestaurantById(id)
                .filter(r -> r.getOwner().getId().equals(userDetails.getId()))
                .map(restaurant -> {
                    if (restaurant.isOpen()) {
                        restaurantService.closeRestaurant(id);
                        redirectAttributes.addFlashAttribute("success", "Το κατάστημα έκλεισε");
                    } else {
                        restaurantService.openRestaurant(id);
                        redirectAttributes.addFlashAttribute("success", "Το κατάστημα άνοιξε");
                    }
                    return "redirect:/owner/dashboard";
                })
                .orElse("redirect:/owner/dashboard");
    }

    // ========== Menu ==========

    @GetMapping("/restaurants/{id}/menu")
    public String menu(@AuthenticationPrincipal CustomUserDetails userDetails,
                        @PathVariable Long id, Model model) {
        return restaurantService.getRestaurantById(id)
                .filter(r -> r.getOwner().getId().equals(userDetails.getId()))
                .map(restaurant -> {
                    model.addAttribute("restaurant", restaurant);
                    model.addAttribute("menuItems", menuItemService.getMenuItemsByRestaurantId(id));
                    return "owner/menu";
                })
                .orElse("redirect:/owner/restaurants");
    }

    @PostMapping("/restaurants/{restaurantId}/menu")
    public String addMenuItem(@AuthenticationPrincipal CustomUserDetails userDetails,
                               @PathVariable Long restaurantId,
                               @RequestParam String name,
                               @RequestParam(required = false) String description,
                               @RequestParam BigDecimal price,
                               @RequestParam(required = false) Boolean available,
                               RedirectAttributes redirectAttributes) {
        return restaurantService.getRestaurantById(restaurantId)
                .filter(r -> r.getOwner().getId().equals(userDetails.getId()))
                .map(restaurant -> {
                    MenuItem item = new MenuItem();
                    item.setName(name);
                    item.setDescription(description);
                    item.setPrice(price);
                    item.setAvailable(available != null && available);
                    menuItemService.createMenuItem(restaurantId, item);
                    redirectAttributes.addFlashAttribute("success", "Το προϊόν προστέθηκε");
                    return "redirect:/owner/restaurants/" + restaurantId + "/menu";
                })
                .orElse("redirect:/owner/restaurants");
    }

    @PostMapping("/restaurants/{restaurantId}/menu/{itemId}/toggle")
    public String toggleMenuItem(@AuthenticationPrincipal CustomUserDetails userDetails,
                                  @PathVariable Long restaurantId,
                                  @PathVariable Long itemId,
                                  RedirectAttributes redirectAttributes) {
        return restaurantService.getRestaurantById(restaurantId)
                .filter(r -> r.getOwner().getId().equals(userDetails.getId()))
                .flatMap(restaurant -> menuItemService.getMenuItemById(itemId))
                .map(item -> {
                    menuItemService.setAvailable(itemId, !item.isAvailable());
                    redirectAttributes.addFlashAttribute("success",
                            item.isAvailable() ? "Το προϊόν απενεργοποιήθηκε" : "Το προϊόν ενεργοποιήθηκε");
                    return "redirect:/owner/restaurants/" + restaurantId + "/menu";
                })
                .orElse("redirect:/owner/restaurants");
    }

    @PostMapping("/restaurants/{restaurantId}/menu/{itemId}/delete")
    public String deleteMenuItem(@AuthenticationPrincipal CustomUserDetails userDetails,
                                  @PathVariable Long restaurantId,
                                  @PathVariable Long itemId,
                                  RedirectAttributes redirectAttributes) {
        return restaurantService.getRestaurantById(restaurantId)
                .filter(r -> r.getOwner().getId().equals(userDetails.getId()))
                .map(restaurant -> {
                    menuItemService.deleteMenuItem(itemId);
                    redirectAttributes.addFlashAttribute("success", "Το προϊόν διαγράφηκε");
                    return "redirect:/owner/restaurants/" + restaurantId + "/menu";
                })
                .orElse("redirect:/owner/restaurants");
    }

    // ========== Orders ==========

    @GetMapping("/orders")
    public String orders(@AuthenticationPrincipal CustomUserDetails userDetails, Model model) {
        List<Restaurant> restaurants = restaurantService.getRestaurantsByOwnerId(userDetails.getId());

        List<Order> pendingOrders = new ArrayList<>();
        List<Order> activeOrders = new ArrayList<>();

        for (Restaurant restaurant : restaurants) {
            List<Order> restaurantOrders = orderService.getOrdersByRestaurantId(restaurant.getId());
            pendingOrders.addAll(restaurantOrders.stream()
                    .filter(o -> o.getStatus() == OrderStatus.PENDING)
                    .collect(Collectors.toList()));
            activeOrders.addAll(restaurantOrders.stream()
                    .filter(o -> o.getStatus() == OrderStatus.ACCEPTED ||
                                 o.getStatus() == OrderStatus.PREPARING ||
                                 o.getStatus() == OrderStatus.READY_FOR_PICKUP ||
                                 o.getStatus() == OrderStatus.OUT_FOR_DELIVERY)
                    .collect(Collectors.toList()));
        }

        model.addAttribute("pendingOrders", pendingOrders);
        model.addAttribute("activeOrders", activeOrders);
        return "owner/orders";
    }

    @GetMapping("/orders/{id}")
    public String orderDetail(@AuthenticationPrincipal CustomUserDetails userDetails,
                               @PathVariable Long id, Model model) {
        return orderService.getOrderById(id)
                .filter(order -> order.getRestaurant().getOwner().getId().equals(userDetails.getId()))
                .map(order -> {
                    model.addAttribute("order", order);
                    return "owner/order-detail";
                })
                .orElse("redirect:/owner/orders");
    }

    @PostMapping("/orders/{id}/accept")
    public String acceptOrder(@AuthenticationPrincipal CustomUserDetails userDetails,
                               @PathVariable Long id,
                               @RequestParam(required = false, defaultValue = "15") Integer prepMinutes,
                               RedirectAttributes redirectAttributes) {
        return orderService.getOrderById(id)
                .filter(order -> order.getRestaurant().getOwner().getId().equals(userDetails.getId()))
                .filter(order -> order.getStatus() == OrderStatus.PENDING)
                .map(order -> {
                    orderService.updateOrderStatus(id, OrderStatus.ACCEPTED);

                    // Calculate total ETA = preparation time + route time
                    int totalMinutes = prepMinutes;

                    // For delivery orders, add route time from OSRM
                    if (order.getOrderType() == OrderType.DELIVERY && order.getDeliveryAddress() != null) {
                        Restaurant restaurant = order.getRestaurant();
                        Address deliveryAddress = order.getDeliveryAddress();

                        if (restaurant.getLatitude() != null && restaurant.getLongitude() != null &&
                            deliveryAddress.getLatitude() != null && deliveryAddress.getLongitude() != null) {

                            DeliveryTimeResult result = deliveryTimeService.calculateDeliveryTime(
                                    restaurant.getLatitude(), restaurant.getLongitude(),
                                    deliveryAddress.getLatitude(), deliveryAddress.getLongitude()
                            );

                            if (result.isSuccess()) {
                                // OSRM returns route time, we add prep time on top
                                int routeMinutes = result.getDurationMinutes();
                                totalMinutes = prepMinutes + routeMinutes;
                                logger.info("Order {}: prep={}min + route={}min = total={}min",
                                        id, prepMinutes, routeMinutes, totalMinutes);
                            } else {
                                logger.warn("OSRM failed for order {}: {}, using prep time only + 15min default",
                                        id, result.getErrorMessage());
                                totalMinutes = prepMinutes + 15; // Default route time
                            }
                        } else {
                            logger.warn("Missing coordinates for order {}, using prep time + 15min default", id);
                            totalMinutes = prepMinutes + 15; // Default route time
                        }
                    }
                    // For pickup orders, just use preparation time

                    LocalDateTime eta = LocalDateTime.now().plusMinutes(totalMinutes);
                    orderService.setEstimatedDeliveryTime(id, eta);
                    redirectAttributes.addFlashAttribute("success", "Η παραγγελία αποδέχτηκε");
                    return "redirect:/owner/dashboard";
                })
                .orElseGet(() -> {
                    redirectAttributes.addFlashAttribute("error", "Δεν μπορεί να γίνει αποδοχή");
                    return "redirect:/owner/dashboard";
                });
    }

    @PostMapping("/orders/{id}/reject")
    public String rejectOrder(@AuthenticationPrincipal CustomUserDetails userDetails,
                               @PathVariable Long id, RedirectAttributes redirectAttributes) {
        return orderService.getOrderById(id)
                .filter(order -> order.getRestaurant().getOwner().getId().equals(userDetails.getId()))
                .filter(order -> order.getStatus() == OrderStatus.PENDING)
                .map(order -> {
                    orderService.updateOrderStatus(id, OrderStatus.REJECTED);
                    redirectAttributes.addFlashAttribute("success", "Η παραγγελία απορρίφθηκε");
                    return "redirect:/owner/dashboard";
                })
                .orElseGet(() -> {
                    redirectAttributes.addFlashAttribute("error", "Δεν μπορεί να γίνει απόρριψη");
                    return "redirect:/owner/dashboard";
                });
    }

    @PostMapping("/orders/{id}/status")
    public String updateOrderStatus(@AuthenticationPrincipal CustomUserDetails userDetails,
                                     @PathVariable Long id,
                                     @RequestParam String status,
                                     RedirectAttributes redirectAttributes) {
        return orderService.getOrderById(id)
                .filter(order -> order.getRestaurant().getOwner().getId().equals(userDetails.getId()))
                .map(order -> {
                    OrderStatus newStatus = OrderStatus.valueOf(status);
                    orderService.updateOrderStatus(id, newStatus);
                    redirectAttributes.addFlashAttribute("success", "Η κατάσταση ενημερώθηκε");
                    return "redirect:/owner/dashboard";
                })
                .orElseGet(() -> {
                    redirectAttributes.addFlashAttribute("error", "Σφάλμα ενημέρωσης");
                    return "redirect:/owner/dashboard";
                });
    }
}
