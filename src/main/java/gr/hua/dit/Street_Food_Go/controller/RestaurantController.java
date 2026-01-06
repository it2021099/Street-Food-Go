package gr.hua.dit.Street_Food_Go.controller;

import gr.hua.dit.Street_Food_Go.dto.RestaurantView;
import gr.hua.dit.Street_Food_Go.model.Restaurant;
import gr.hua.dit.Street_Food_Go.service.RestaurantService;
import gr.hua.dit.Street_Food_Go.util.RestaurantMapper;
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
@RequestMapping("/api/restaurants")
@Tag(name = "Restaurants", description = "Restaurant management endpoints")
public class RestaurantController {

    private final RestaurantService restaurantService;

    public RestaurantController(final RestaurantService restaurantService) {
        if (restaurantService == null) {
            throw new NullPointerException("restaurantService cannot be null");
        }
        this.restaurantService = restaurantService;
    }

    @PostMapping("/owner/{ownerId}")
    @PreAuthorize("hasRole('RESTAURANT_OWNER')")
    @Operation(summary = "Create a new restaurant", description = "Creates a new restaurant for the specified owner")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Restaurant created successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid request"),
            @ApiResponse(responseCode = "403", description = "Access denied")
    })
    public ResponseEntity<RestaurantView> createRestaurant(
            @Parameter(description = "Owner ID") @PathVariable Long ownerId,
            @RequestBody Restaurant restaurant) {
        try {
            Restaurant createdRestaurant = restaurantService.createRestaurant(ownerId, restaurant);
            return new ResponseEntity<>(RestaurantMapper.toView(createdRestaurant), HttpStatus.CREATED);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @GetMapping
    @Operation(summary = "Get all restaurants", description = "Returns a list of all restaurants")
    @ApiResponse(responseCode = "200", description = "List of restaurants")
    public ResponseEntity<List<RestaurantView>> getAllRestaurants() {
        List<Restaurant> restaurants = restaurantService.getAllRestaurants();
        return ResponseEntity.ok(RestaurantMapper.toViewList(restaurants));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get restaurant by ID", description = "Returns a single restaurant by its ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Restaurant found"),
            @ApiResponse(responseCode = "404", description = "Restaurant not found")
    })
    public ResponseEntity<RestaurantView> getRestaurantById(
            @Parameter(description = "Restaurant ID") @PathVariable Long id) {
        return restaurantService.getRestaurantById(id)
                .map(restaurant -> ResponseEntity.ok(RestaurantMapper.toView(restaurant)))
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/owner/{ownerId}")
    @Operation(summary = "Get restaurants by owner", description = "Returns all restaurants owned by the specified user")
    @ApiResponse(responseCode = "200", description = "List of restaurants")
    public ResponseEntity<List<RestaurantView>> getRestaurantsByOwnerId(
            @Parameter(description = "Owner ID") @PathVariable Long ownerId) {
        List<Restaurant> restaurants = restaurantService.getRestaurantsByOwnerId(ownerId);
        return ResponseEntity.ok(RestaurantMapper.toViewList(restaurants));
    }

    @GetMapping("/open")
    @Operation(summary = "Get open restaurants", description = "Returns all currently open restaurants")
    @ApiResponse(responseCode = "200", description = "List of open restaurants")
    public ResponseEntity<List<RestaurantView>> getOpenRestaurants() {
        List<Restaurant> restaurants = restaurantService.getOpenRestaurants();
        return ResponseEntity.ok(RestaurantMapper.toViewList(restaurants));
    }

    @GetMapping("/cuisine/{cuisineType}")
    @Operation(summary = "Get restaurants by cuisine type", description = "Returns restaurants filtered by cuisine type")
    @ApiResponse(responseCode = "200", description = "List of restaurants")
    public ResponseEntity<List<RestaurantView>> getRestaurantsByCuisineType(
            @Parameter(description = "Cuisine type") @PathVariable String cuisineType) {
        List<Restaurant> restaurants = restaurantService.getRestaurantsByCuisineType(cuisineType);
        return ResponseEntity.ok(RestaurantMapper.toViewList(restaurants));
    }

    @GetMapping("/search")
    @Operation(summary = "Search restaurants", description = "Searches restaurants by keyword in name or cuisine type")
    @ApiResponse(responseCode = "200", description = "List of matching restaurants")
    public ResponseEntity<List<RestaurantView>> searchRestaurants(
            @Parameter(description = "Search keyword") @RequestParam String keyword) {
        List<Restaurant> restaurants = restaurantService.searchRestaurants(keyword);
        return ResponseEntity.ok(RestaurantMapper.toViewList(restaurants));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('RESTAURANT_OWNER')")
    @Operation(summary = "Update restaurant", description = "Updates an existing restaurant")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Restaurant updated"),
            @ApiResponse(responseCode = "404", description = "Restaurant not found")
    })
    public ResponseEntity<RestaurantView> updateRestaurant(
            @Parameter(description = "Restaurant ID") @PathVariable Long id,
            @RequestBody Restaurant restaurant) {
        try {
            Restaurant updatedRestaurant = restaurantService.updateRestaurant(id, restaurant);
            return ResponseEntity.ok(RestaurantMapper.toView(updatedRestaurant));
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @PatchMapping("/{id}/open")
    @PreAuthorize("hasRole('RESTAURANT_OWNER')")
    @Operation(summary = "Open restaurant", description = "Sets the restaurant status to open")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Restaurant opened"),
            @ApiResponse(responseCode = "404", description = "Restaurant not found")
    })
    public ResponseEntity<RestaurantView> openRestaurant(
            @Parameter(description = "Restaurant ID") @PathVariable Long id) {
        try {
            Restaurant restaurant = restaurantService.openRestaurant(id);
            return ResponseEntity.ok(RestaurantMapper.toView(restaurant));
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @PatchMapping("/{id}/close")
    @PreAuthorize("hasRole('RESTAURANT_OWNER')")
    @Operation(summary = "Close restaurant", description = "Sets the restaurant status to closed")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Restaurant closed"),
            @ApiResponse(responseCode = "404", description = "Restaurant not found")
    })
    public ResponseEntity<RestaurantView> closeRestaurant(
            @Parameter(description = "Restaurant ID") @PathVariable Long id) {
        try {
            Restaurant restaurant = restaurantService.closeRestaurant(id);
            return ResponseEntity.ok(RestaurantMapper.toView(restaurant));
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('RESTAURANT_OWNER')")
    @Operation(summary = "Delete restaurant", description = "Deletes a restaurant")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Restaurant deleted"),
            @ApiResponse(responseCode = "404", description = "Restaurant not found")
    })
    public ResponseEntity<Void> deleteRestaurant(
            @Parameter(description = "Restaurant ID") @PathVariable Long id) {
        try {
            restaurantService.deleteRestaurant(id);
            return ResponseEntity.noContent().build();
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }
}
