package gr.hua.dit.Street_Food_Go.util;

import gr.hua.dit.Street_Food_Go.dto.MenuItemView;
import gr.hua.dit.Street_Food_Go.model.MenuItem;

import java.util.List;
import java.util.stream.Collectors;

public class MenuItemMapper {

    private MenuItemMapper() {
    }

    public static MenuItemView toView(MenuItem menuItem) {
        if (menuItem == null) {
            return null;
        }
        return new MenuItemView(
                menuItem.getId(),
                menuItem.getName(),
                menuItem.getDescription(),
                menuItem.getPrice(),
                menuItem.isAvailable(),
                menuItem.getRestaurant() != null ? menuItem.getRestaurant().getId() : null,
                menuItem.getRestaurant() != null ? menuItem.getRestaurant().getName() : null
        );
    }

    public static List<MenuItemView> toViewList(List<MenuItem> menuItems) {
        if (menuItems == null) {
            return null;
        }
        return menuItems.stream()
                .map(MenuItemMapper::toView)
                .collect(Collectors.toList());
    }
}
