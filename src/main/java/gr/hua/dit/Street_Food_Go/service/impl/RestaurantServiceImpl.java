package gr.hua.dit.Street_Food_Go.service.impl;

import gr.hua.dit.Street_Food_Go.model.Restaurant;
import gr.hua.dit.Street_Food_Go.model.User;
import gr.hua.dit.Street_Food_Go.repository.RestaurantRepository;
import gr.hua.dit.Street_Food_Go.repository.UserRepository;
import gr.hua.dit.Street_Food_Go.service.RestaurantService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class RestaurantServiceImpl implements RestaurantService {

    private final RestaurantRepository restaurantRepository;
    private final UserRepository userRepository;

    public RestaurantServiceImpl(final RestaurantRepository restaurantRepository, final UserRepository userRepository) {
        if (restaurantRepository == null) {
            throw new NullPointerException("restaurantRepository cannot be null");
        }
        if (userRepository == null) {
            throw new NullPointerException("userRepository cannot be null");
        }
        this.restaurantRepository = restaurantRepository;
        this.userRepository = userRepository;
    }

    @Override
    public Restaurant createRestaurant(Long ownerId, Restaurant restaurant) {
        User owner = userRepository.findById(ownerId)
                .orElseThrow(() -> new RuntimeException("Owner not found with id: " + ownerId));
        restaurant.setOwner(owner);
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
