package gr.hua.dit.Street_Food_Go.dto;

public class AuthResponse {

    private String token;
    private UserView user;

    public AuthResponse() {
    }

    public AuthResponse(String token, UserView user) {
        this.token = token;
        this.user = user;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public UserView getUser() {
        return user;
    }

    public void setUser(UserView user) {
        this.user = user;
    }
}
