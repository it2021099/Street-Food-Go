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

    public String getBusinessName() {
        return businessName;
    }

    public void setBusinessName(String businessName) {
        this.businessName = businessName;
    }

    public boolean isOpen() {
        return isOpen;
    }

    public void setOpen(boolean open) {
        isOpen = open;
    }

    public List<MenuItem> getMenu() {
        return menu;
    }

    public void setMenu(List<MenuItem> menu) {
        this.menu = menu;
    }
}
