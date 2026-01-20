package gr.hua.dit.Street_Food_Go.service.impl;

import gr.hua.dit.Street_Food_Go.model.Restaurant;
import gr.hua.dit.Street_Food_Go.model.User;
import gr.hua.dit.Street_Food_Go.repository.RestaurantRepository;
import gr.hua.dit.Street_Food_Go.repository.UserRepository;
import gr.hua.dit.Street_Food_Go.service.RestaurantService;
import gr.hua.dit.Street_Food_Go.service.external.GeocodingResult;
import gr.hua.dit.Street_Food_Go.service.external.GeocodingService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class RestaurantServiceImpl implements RestaurantService {

    private static final Logger logger = LoggerFactory.getLogger(RestaurantServiceImpl.class);

    private final RestaurantRepository restaurantRepository;
    private final UserRepository userRepository;
    private final GeocodingService geocodingService;

    public RestaurantServiceImpl(
            final RestaurantRepository restaurantRepository,
            final UserRepository userRepository,
            final GeocodingService geocodingService) {
        if (restaurantRepository == null) {
            throw new NullPointerException("restaurantRepository cannot be null");
        }
        if (userRepository == null) {
            throw new NullPointerException("userRepository cannot be null");
        }
        if (geocodingService == null) {
            throw new NullPointerException("geocodingService cannot be null");
        }
        this.restaurantRepository = restaurantRepository;
        this.userRepository = userRepository;
        this.geocodingService = geocodingService;
    }

    /**
     * Geocode the restaurant address if coordinates are not provided.
     */
    private void geocodeIfNeeded(Restaurant restaurant) {
        if (restaurant.getLatitude() == null || restaurant.getLongitude() == null) {
            String address = restaurant.getAddress();
            if (address != null && !address.isBlank()) {
                logger.info("Geocoding restaurant address: {}", address);

                // Append Greece to the address for better results
                GeocodingResult result = geocodingService.geocodeAddress(address + ", Greece");

                if (result.isSuccess()) {
                    restaurant.setLatitude(result.getLatitude());
                    restaurant.setLongitude(result.getLongitude());
                    logger.info("Geocoded to lat={}, lon={}", result.getLatitude(), result.getLongitude());
                } else {
                    logger.warn("Geocoding failed: {}", result.getErrorMessage());
                }
            }
        }
    }

    @Override
    public Restaurant createRestaurant(Long ownerId, Restaurant restaurant) {
        User owner = userRepository.findById(ownerId)
                .orElseThrow(() -> new RuntimeException("Owner not found with id: " + ownerId));
        restaurant.setOwner(owner);

        // Auto-geocode if coordinates not provided
        geocodeIfNeeded(restaurant);

        return restaurantRepository.save(restaurant);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Restaurant> getAllRestaurants() {
        return restaurantRepository.findAll();
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Restaurant> getRestaurantById(Long id) {
        return restaurantRepository.findById(id);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Restaurant> getRestaurantsByOwnerId(Long ownerId) {
        return restaurantRepository.findByOwnerId(ownerId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Restaurant> getOpenRestaurants() {
        return restaurantRepository.findByIsOpenTrue();
    }

    @Override
    @Transactional(readOnly = true)
    public List<Restaurant> getRestaurantsByCuisineType(String cuisineType) {
        return restaurantRepository.findByCuisineType(cuisineType);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Restaurant> searchRestaurants(String keyword) {
        return restaurantRepository.searchByKeyword(keyword);
    }

    @Override
    public Restaurant updateRestaurant(Long id, Restaurant restaurantDetails) {
        Restaurant restaurant = restaurantRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Restaurant not found with id: " + id));

        restaurant.setName(restaurantDetails.getName());
        restaurant.setDescription(restaurantDetails.getDescription());
        restaurant.setCuisineType(restaurantDetails.getCuisineType());
        restaurant.setAddress(restaurantDetails.getAddress());
        restaurant.setLatitude(restaurantDetails.getLatitude());
        restaurant.setLongitude(restaurantDetails.getLongitude());
        restaurant.setMinimumOrderValue(restaurantDetails.getMinimumOrderValue());

        // Auto-geocode if coordinates not provided
        geocodeIfNeeded(restaurant);

        return restaurantRepository.save(restaurant);
    }

    @Override
    public Restaurant openRestaurant(Long id) {
        Restaurant restaurant = restaurantRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Restaurant not found with id: " + id));
        restaurant.setOpen(true);
        return restaurantRepository.save(restaurant);
    }

    @Override
    public Restaurant closeRestaurant(Long id) {
        Restaurant restaurant = restaurantRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Restaurant not found with id: " + id));
        restaurant.setOpen(false);
        return restaurantRepository.save(restaurant);
    }

    @Override
    public void deleteRestaurant(Long id) {
        restaurantRepository.deleteById(id);
    }
}
