package gr.hua.dit.Street_Food_Go.service;

import gr.hua.dit.Street_Food_Go.model.Restaurant;

import java.util.List;
import java.util.Optional;

/**
 * Service interface for managing {@code Restaurant} entities.
 */
public interface RestaurantService {

    Restaurant createRestaurant(Long ownerId, Restaurant restaurant);

    List<Restaurant> getAllRestaurants();

    Optional<Restaurant> getRestaurantById(Long id);

    List<Restaurant> getRestaurantsByOwnerId(Long ownerId);

    List<Restaurant> getOpenRestaurants();

    List<Restaurant> getRestaurantsByCuisineType(String cuisineType);

    List<Restaurant> searchRestaurants(String keyword);

    Restaurant updateRestaurant(Long id, Restaurant restaurantDetails);

    Restaurant openRestaurant(Long id);

    Restaurant closeRestaurant(Long id);

    void deleteRestaurant(Long id);
}
