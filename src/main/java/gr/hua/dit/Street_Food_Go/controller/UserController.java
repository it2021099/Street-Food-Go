package gr.hua.dit.Street_Food_Go.controller;

import gr.hua.dit.Street_Food_Go.dto.UserView;
import gr.hua.dit.Street_Food_Go.model.Role;
import gr.hua.dit.Street_Food_Go.model.User;
import gr.hua.dit.Street_Food_Go.service.UserService;
import gr.hua.dit.Street_Food_Go.util.UserMapper;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserService userService;

    public UserController(final UserService userService) {
        if (userService == null) {
            throw new NullPointerException("userService cannot be null");
        }
        this.userService = userService;
    }

    @PostMapping
    @PreAuthorize("hasRole('RESTAURANT_OWNER')")
    public ResponseEntity<UserView> createUser(@RequestBody User user) {
        User createdUser = userService.createUser(user);
        return new ResponseEntity<>(UserMapper.toView(createdUser), HttpStatus.CREATED);
    }

    @GetMapping
    @PreAuthorize("hasRole('RESTAURANT_OWNER')")
    public ResponseEntity<List<UserView>> getAllUsers() {
        List<User> users = userService.getAllUsers();
        return ResponseEntity.ok(UserMapper.toViewList(users));
    }

    @GetMapping("/{id}")
    public ResponseEntity<UserView> getUserById(@PathVariable Long id) {
        return userService.getUserById(id)
                .map(user -> ResponseEntity.ok(UserMapper.toView(user)))
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/username/{username}")
    public ResponseEntity<UserView> getUserByUsername(@PathVariable String username) {
        return userService.getUserByUsername(username)
                .map(user -> ResponseEntity.ok(UserMapper.toView(user)))
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/role/{role}")
    @PreAuthorize("hasRole('RESTAURANT_OWNER')")
    public ResponseEntity<List<UserView>> getUsersByRole(@PathVariable Role role) {
        List<User> users = userService.getUsersByRole(role);
        return ResponseEntity.ok(UserMapper.toViewList(users));
    }

    @PutMapping("/{id}")
    public ResponseEntity<UserView> updateUser(@PathVariable Long id, @RequestBody User user) {
        try {
            User updatedUser = userService.updateUser(id, user);
            return ResponseEntity.ok(UserMapper.toView(updatedUser));
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('RESTAURANT_OWNER')")
    public ResponseEntity<Void> deleteUser(@PathVariable Long id) {
        try {
            userService.deleteUser(id);
            return ResponseEntity.noContent().build();
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @PatchMapping("/{id}/enable")
    @PreAuthorize("hasRole('RESTAURANT_OWNER')")
    public ResponseEntity<UserView> enableUser(@PathVariable Long id) {
        try {
            User user = userService.enableUser(id);
            return ResponseEntity.ok(UserMapper.toView(user));
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @PatchMapping("/{id}/disable")
    @PreAuthorize("hasRole('RESTAURANT_OWNER')")
    public ResponseEntity<UserView> disableUser(@PathVariable Long id) {
        try {
            User user = userService.disableUser(id);
            return ResponseEntity.ok(UserMapper.toView(user));
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }
}
