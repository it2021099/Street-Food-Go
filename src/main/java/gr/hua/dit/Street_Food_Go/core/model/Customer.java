package gr.hua.dit.Street_Food_Go.core.model;

import java.util.List;

public class Customer extends Person {

    private List<String> addresses;

    public Customer() {}

    public Customer(Long id, String firstName, String lastName, String email,
                    String username, String password, List<String> addresses) {
        super(id, firstName, lastName, email,null, username, password);
        this.addresses = addresses;
    }

    // getters & setters

    public List<String> getAddresses() {
        return addresses;
    }

    public void setAddresses(List<String> addresses) {
        this.addresses = addresses;
    }
}
