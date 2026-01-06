package gr.hua.dit.Street_Food_Go.controller;

import gr.hua.dit.Street_Food_Go.dto.MenuItemView;
import gr.hua.dit.Street_Food_Go.model.MenuItem;
import gr.hua.dit.Street_Food_Go.service.MenuItemService;
import gr.hua.dit.Street_Food_Go.util.MenuItemMapper;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/menu-items")
public class MenuItemController {

    private final MenuItemService menuItemService;

    public MenuItemController(final MenuItemService menuItemService) {
        if (menuItemService == null) {
            throw new NullPointerException("menuItemService cannot be null");
        }
        this.menuItemService = menuItemService;
    }

    @PostMapping("/restaurant/{restaurantId}")
    @PreAuthorize("hasRole('RESTAURANT_OWNER')")
    public ResponseEntity<MenuItemView> createMenuItem(@PathVariable Long restaurantId, @RequestBody MenuItem menuItem) {
        try {
            MenuItem createdMenuItem = menuItemService.createMenuItem(restaurantId, menuItem);
            return new ResponseEntity<>(MenuItemMapper.toView(createdMenuItem), HttpStatus.CREATED);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @GetMapping("/restaurant/{restaurantId}")
    public ResponseEntity<List<MenuItemView>> getMenuItemsByRestaurantId(@PathVariable Long restaurantId) {
        List<MenuItem> menuItems = menuItemService.getMenuItemsByRestaurantId(restaurantId);
        return ResponseEntity.ok(MenuItemMapper.toViewList(menuItems));
    }

    @GetMapping("/restaurant/{restaurantId}/available")
    public ResponseEntity<List<MenuItemView>> getAvailableMenuItems(@PathVariable Long restaurantId) {
        List<MenuItem> menuItems = menuItemService.getAvailableMenuItems(restaurantId);
        return ResponseEntity.ok(MenuItemMapper.toViewList(menuItems));
    }

    @GetMapping("/{id}")
    public ResponseEntity<MenuItemView> getMenuItemById(@PathVariable Long id) {
        return menuItemService.getMenuItemById(id)
                .map(menuItem -> ResponseEntity.ok(MenuItemMapper.toView(menuItem)))
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/search")
    public ResponseEntity<List<MenuItemView>> searchMenuItems(@RequestParam String name) {
        List<MenuItem> menuItems = menuItemService.searchMenuItems(name);
        return ResponseEntity.ok(MenuItemMapper.toViewList(menuItems));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('RESTAURANT_OWNER')")
    public ResponseEntity<MenuItemView> updateMenuItem(@PathVariable Long id, @RequestBody MenuItem menuItem) {
        try {
            MenuItem updatedMenuItem = menuItemService.updateMenuItem(id, menuItem);
            return ResponseEntity.ok(MenuItemMapper.toView(updatedMenuItem));
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @PatchMapping("/{id}/available/{available}")
    @PreAuthorize("hasRole('RESTAURANT_OWNER')")
    public ResponseEntity<MenuItemView> setAvailable(@PathVariable Long id, @PathVariable boolean available) {
        try {
            MenuItem menuItem = menuItemService.setAvailable(id, available);
            return ResponseEntity.ok(MenuItemMapper.toView(menuItem));
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('RESTAURANT_OWNER')")
    public ResponseEntity<Void> deleteMenuItem(@PathVariable Long id) {
        try {
            menuItemService.deleteMenuItem(id);
            return ResponseEntity.noContent().build();
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }
}
