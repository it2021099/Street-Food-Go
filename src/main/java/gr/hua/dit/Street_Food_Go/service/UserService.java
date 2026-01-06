package gr.hua.dit.Street_Food_Go.service;

import gr.hua.dit.Street_Food_Go.model.Role;
import gr.hua.dit.Street_Food_Go.model.User;

import java.util.List;
import java.util.Optional;

/**
 * Service interface for managing {@code User} entities.
 */
public interface UserService {

    User createUser(User user);

    List<User> getAllUsers();

    Optional<User> getUserById(Long id);

    Optional<User> getUserByUsername(String username);

    Optional<User> getUserByEmail(String email);

    List<User> getUsersByRole(Role role);

    User updateUser(Long id, User userDetails);

    void deleteUser(Long id);

    User enableUser(Long id);

    User disableUser(Long id);

    boolean existsByUsername(String username);

    boolean existsByEmail(String email);
}
