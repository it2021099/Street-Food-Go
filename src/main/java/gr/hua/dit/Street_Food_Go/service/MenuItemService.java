package gr.hua.dit.Street_Food_Go.service;

import gr.hua.dit.Street_Food_Go.model.MenuItem;

import java.util.List;
import java.util.Optional;

/**
 * Service interface for managing {@code MenuItem} entities.
 */
public interface MenuItemService {

    MenuItem createMenuItem(Long restaurantId, MenuItem menuItem);

    List<MenuItem> getMenuItemsByRestaurantId(Long restaurantId);

    List<MenuItem> getAvailableMenuItems(Long restaurantId);

    Optional<MenuItem> getMenuItemById(Long id);

    List<MenuItem> searchMenuItems(String name);

    MenuItem updateMenuItem(Long id, MenuItem menuItemDetails);

    MenuItem setAvailable(Long id, boolean available);

    void deleteMenuItem(Long id);
}
