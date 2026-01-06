package gr.hua.dit.Street_Food_Go.dto;

import gr.hua.dit.Street_Food_Go.model.Role;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public class RegisterRequest {

    @NotBlank(message = "Το όνομα χρήστη είναι υποχρεωτικό")
    @Size(min = 3, max = 50, message = "Το όνομα χρήστη πρέπει να είναι 3-50 χαρακτήρες")
    private String username;

    @NotBlank(message = "Το email είναι υποχρεωτικό")
    @Email(message = "Μη έγκυρη μορφή email")
    private String email;

    @NotBlank(message = "Ο κωδικός είναι υποχρεωτικός")
    @Size(min = 6, message = "Ο κωδικός πρέπει να είναι τουλάχιστον 6 χαρακτήρες")
    private String password;

    @NotNull(message = "Ο ρόλος είναι υποχρεωτικός")
    private Role role;

    public RegisterRequest() {
    }

    public RegisterRequest(String username, String email, String password, Role role) {
        this.username = username;
        this.email = email;
        this.password = password;
        this.role = role;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public Role getRole() {
        return role;
    }

    public void setRole(Role role) {
        this.role = role;
    }
}
