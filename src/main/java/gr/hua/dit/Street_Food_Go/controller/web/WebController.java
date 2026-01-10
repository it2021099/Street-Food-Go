package gr.hua.dit.Street_Food_Go.controller.web;

import gr.hua.dit.Street_Food_Go.model.Restaurant;
import gr.hua.dit.Street_Food_Go.model.Role;
import gr.hua.dit.Street_Food_Go.model.User;
import gr.hua.dit.Street_Food_Go.service.MenuItemService;
import gr.hua.dit.Street_Food_Go.service.RestaurantService;
import gr.hua.dit.Street_Food_Go.service.UserService;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
public class WebController {

    private final RestaurantService restaurantService;
    private final MenuItemService menuItemService;
    private final UserService userService;
    private final PasswordEncoder passwordEncoder;

    public WebController(final RestaurantService restaurantService,
                         final MenuItemService menuItemService,
                         final UserService userService,
                         final PasswordEncoder passwordEncoder) {
        if (restaurantService == null) {
            throw new NullPointerException("restaurantService cannot be null");
        }
        if (menuItemService == null) {
            throw new NullPointerException("menuItemService cannot be null");
        }
        if (userService == null) {
            throw new NullPointerException("userService cannot be null");
        }
        if (passwordEncoder == null) {
            throw new NullPointerException("passwordEncoder cannot be null");
        }
        this.restaurantService = restaurantService;
        this.menuItemService = menuItemService;
        this.userService = userService;
        this.passwordEncoder = passwordEncoder;
    }

    @GetMapping("/")
    public String index(@RequestParam(required = false) String search, Model model) {
        List<Restaurant> restaurants;
        if (search != null && !search.trim().isEmpty()) {
            restaurants = restaurantService.searchRestaurants(search.trim());
            model.addAttribute("searchQuery", search);
        } else {
            restaurants = restaurantService.getAllRestaurants();
        }
        model.addAttribute("restaurants", restaurants);
        return "index";
    }

    // ==================== LOGIN ENDPOINTS ====================

    @GetMapping("/login")
    public String loginClassChoice() {
        return "ClassChoice_LoginPressed";
    }

    @GetMapping("/login/customer")
    public String loginCustomer() {
        return "Regular_Login";
    }

    @GetMapping("/login/restaurant")
    public String loginRestaurant() {
        return "Restaurant_Login";
    }

    @GetMapping("/login-success")
    public String loginSuccess(Authentication authentication) {
        if (authentication.getAuthorities().contains(new SimpleGrantedAuthority("ROLE_RESTAURANT_OWNER"))) {
            return "redirect:/owner/dashboard";
        } else if (authentication.getAuthorities().contains(new SimpleGrantedAuthority("ROLE_CUSTOMER"))) {
            return "redirect:/customer/dashboard";
        }
        return "redirect:/";
    }

    // ==================== REGISTER ENDPOINTS ====================

    @GetMapping("/register")
    public String registerClassChoice() {
        return "ClassChoice_RegisterPressed";
    }

    @GetMapping("/register/customer")
    public String registerCustomerForm() {
        return "Regular_Register";
    }

    @GetMapping("/register/restaurant")
    public String registerRestaurantForm() {
        return "Restaurant_Register";
    }

    @PostMapping("/register/customer")
    public String registerCustomer(@RequestParam String username,
                                   @RequestParam String email,
                                   @RequestParam String password,
                                   @RequestParam(required = false) String address,
                                   RedirectAttributes redirectAttributes) {
        if (userService.existsByUsername(username)) {
            redirectAttributes.addFlashAttribute("error", "Username already exists");
            return "redirect:/register/customer";
        }

        if (userService.existsByEmail(email)) {
            redirectAttributes.addFlashAttribute("error", "Email already exists");
            return "redirect:/register/customer";
        }

        User user = new User();
        user.setUsername(username);
        user.setEmail(email);
        user.setPassword(passwordEncoder.encode(password));
        user.setRole(Role.CUSTOMER);
        user.setEnabled(true);

        userService.createUser(user);

        return "redirect:/login/customer?success=true";
    }

    @PostMapping("/register/restaurant")
    public String registerRestaurant(@RequestParam String username,
                                     @RequestParam String email,
                                     @RequestParam String password,
                                     @RequestParam(required = false) String address,
                                     RedirectAttributes redirectAttributes) {
        if (userService.existsByUsername(username)) {
            redirectAttributes.addFlashAttribute("error", "Username already exists");
            return "redirect:/register/restaurant";
        }

        if (userService.existsByEmail(email)) {
            redirectAttributes.addFlashAttribute("error", "Email already exists");
            return "redirect:/register/restaurant";
        }

        User user = new User();
        user.setUsername(username);
        user.setEmail(email);
        user.setPassword(passwordEncoder.encode(password));
        user.setRole(Role.RESTAURANT_OWNER);
        user.setEnabled(true);

        userService.createUser(user);

        return "redirect:/login/restaurant?success=true";
    }

    @GetMapping("/restaurant/{id}")
    public String restaurantDetail(@PathVariable Long id, Model model) {
        return restaurantService.getRestaurantById(id)
                .map(restaurant -> {
                    model.addAttribute("restaurant", restaurant);
                    model.addAttribute("menuItems", menuItemService.getAvailableMenuItems(id));
                    return "restaurant-detail";
                })
                .orElse("redirect:/");
    }
}
