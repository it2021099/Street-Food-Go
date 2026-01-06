package gr.hua.dit.Street_Food_Go.util;

import gr.hua.dit.Street_Food_Go.dto.UserView;
import gr.hua.dit.Street_Food_Go.model.User;

import java.util.List;
import java.util.stream.Collectors;

public class UserMapper {

    private UserMapper() {
    }

    public static UserView toView(User user) {
        if (user == null) {
            return null;
        }
        return new UserView(
                user.getId(),
                user.getUsername(),
                user.getEmail(),
                user.getRole(),
                user.isEnabled(),
                user.getCreatedAt()
        );
    }

    public static List<UserView> toViewList(List<User> users) {
        if (users == null) {
            return null;
        }
        return users.stream()
                .map(UserMapper::toView)
                .collect(Collectors.toList());
    }
}
