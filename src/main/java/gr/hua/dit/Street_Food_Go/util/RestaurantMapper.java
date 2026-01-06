package gr.hua.dit.Street_Food_Go.util;

import gr.hua.dit.Street_Food_Go.dto.RestaurantView;
import gr.hua.dit.Street_Food_Go.model.Restaurant;

import java.util.List;
import java.util.stream.Collectors;

public class RestaurantMapper {

    private RestaurantMapper() {
    }

    public static RestaurantView toView(Restaurant restaurant) {
        if (restaurant == null) {
            return null;
        }
        RestaurantView view = new RestaurantView();
        view.setId(restaurant.getId());
        view.setName(restaurant.getName());
        view.setDescription(restaurant.getDescription());
        view.setCuisineType(restaurant.getCuisineType());
        view.setAddress(restaurant.getAddress());
        view.setLatitude(restaurant.getLatitude());
        view.setLongitude(restaurant.getLongitude());
        view.setOpen(restaurant.isOpen());
        view.setMinimumOrderValue(restaurant.getMinimumOrderValue());

        if (restaurant.getOwner() != null) {
            view.setOwnerId(restaurant.getOwner().getId());
            view.setOwnerUsername(restaurant.getOwner().getUsername());
        }

        if (restaurant.getMenuItems() != null) {
            view.setMenuItems(MenuItemMapper.toViewList(restaurant.getMenuItems()));
        }

        return view;
    }

    public static RestaurantView toViewWithoutMenuItems(Restaurant restaurant) {
        if (restaurant == null) {
            return null;
        }
        RestaurantView view = new RestaurantView();
        view.setId(restaurant.getId());
        view.setName(restaurant.getName());
        view.setDescription(restaurant.getDescription());
        view.setCuisineType(restaurant.getCuisineType());
        view.setAddress(restaurant.getAddress());
        view.setLatitude(restaurant.getLatitude());
        view.setLongitude(restaurant.getLongitude());
        view.setOpen(restaurant.isOpen());
        view.setMinimumOrderValue(restaurant.getMinimumOrderValue());

        if (restaurant.getOwner() != null) {
            view.setOwnerId(restaurant.getOwner().getId());
            view.setOwnerUsername(restaurant.getOwner().getUsername());
        }

        return view;
    }

    public static List<RestaurantView> toViewList(List<Restaurant> restaurants) {
        if (restaurants == null) {
            return null;
        }
        return restaurants.stream()
                .map(RestaurantMapper::toViewWithoutMenuItems)
                .collect(Collectors.toList());
    }
}
