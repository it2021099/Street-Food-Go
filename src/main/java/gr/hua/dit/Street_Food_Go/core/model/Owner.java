package gr.hua.dit.Street_Food_Go.core.model;

import java.awt.*;
import java.util.List;

public class Owner extends Person {

    private String businessName;
    private boolean isOpen;
    private List<MenuItem> menu;

    public Owner() {}

    public Owner(Long id, String firstName, String lastName, String email,
                 String username, String password, String businessName,
                 boolean isOpen, List<MenuItem> menu) {
        super(id, firstName, lastName, email,null, username, password);
        this.businessName = businessName;
        this.isOpen = isOpen;
        this.menu = menu;
    }

    // getters & setters
}
