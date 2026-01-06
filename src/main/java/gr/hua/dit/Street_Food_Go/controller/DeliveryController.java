package gr.hua.dit.Street_Food_Go.controller;

import gr.hua.dit.Street_Food_Go.model.Address;
import gr.hua.dit.Street_Food_Go.model.Restaurant;
import gr.hua.dit.Street_Food_Go.service.AddressService;
import gr.hua.dit.Street_Food_Go.service.RestaurantService;
import gr.hua.dit.Street_Food_Go.service.external.DeliveryTimeResult;
import gr.hua.dit.Street_Food_Go.service.external.DeliveryTimeService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * REST Controller for delivery-related operations.
 * Exposes the external OSRM routing service through a clean API.
 */
@RestController
@RequestMapping("/api/delivery")
public class DeliveryController {

    private final DeliveryTimeService deliveryTimeService;
    private final RestaurantService restaurantService;
    private final AddressService addressService;

    public DeliveryController(
            final DeliveryTimeService deliveryTimeService,
            final RestaurantService restaurantService,
            final AddressService addressService) {
        if (deliveryTimeService == null) {
            throw new NullPointerException("deliveryTimeService cannot be null");
        }
        if (restaurantService == null) {
            throw new NullPointerException("restaurantService cannot be null");
        }
        if (addressService == null) {
            throw new NullPointerException("addressService cannot be null");
        }
        this.deliveryTimeService = deliveryTimeService;
        this.restaurantService = restaurantService;
        this.addressService = addressService;
    }

    /**
     * Estimates delivery time between a restaurant and a delivery address.
     * Uses the external OSRM service to calculate the route.
     *
     * @param restaurantId The ID of the restaurant (origin)
     * @param addressId    The ID of the delivery address (destination)
     * @return Estimated delivery time in minutes and distance in km
     */
    @GetMapping("/estimate")
    public ResponseEntity<Map<String, Object>> estimateDeliveryTime(
            @RequestParam Long restaurantId,
            @RequestParam Long addressId) {

        Restaurant restaurant = restaurantService.getRestaurantById(restaurantId)
                .orElse(null);
        if (restaurant == null) {
            return ResponseEntity.badRequest().body(Map.of("error", "Restaurant not found"));
        }

        Address address = addressService.getAddressById(addressId)
                .orElse(null);
        if (address == null) {
            return ResponseEntity.badRequest().body(Map.of("error", "Address not found"));
        }

        DeliveryTimeResult result = deliveryTimeService.calculateDeliveryTime(
                restaurant.getLatitude(),
                restaurant.getLongitude(),
                address.getLatitude(),
                address.getLongitude()
        );

        Map<String, Object> response = new HashMap<>();
        if (result.isSuccess()) {
            response.put("success", true);
            response.put("durationMinutes", result.getDurationMinutes());
            response.put("distanceKm", result.getDistanceKm());
            response.put("restaurantName", restaurant.getName());
            response.put("deliveryAddress", address.getStreetAddress() + ", " + address.getCity());
        } else {
            response.put("success", false);
            response.put("error", result.getErrorMessage());
        }

        return ResponseEntity.ok(response);
    }

}
