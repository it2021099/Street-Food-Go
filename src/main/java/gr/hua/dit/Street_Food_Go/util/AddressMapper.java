package gr.hua.dit.Street_Food_Go.util;

import gr.hua.dit.Street_Food_Go.dto.AddressView;
import gr.hua.dit.Street_Food_Go.model.Address;

import java.util.List;
import java.util.stream.Collectors;

public class AddressMapper {

    private AddressMapper() {
    }

    public static AddressView toView(Address address) {
        if (address == null) {
            return null;
        }
        return new AddressView(
                address.getId(),
                address.getUser() != null ? address.getUser().getId() : null,
                address.getStreetAddress(),
                address.getCity(),
                address.getPostalCode(),
                address.getLatitude(),
                address.getLongitude(),
                address.isDefault()
        );
    }

    public static List<AddressView> toViewList(List<Address> addresses) {
        if (addresses == null) {
            return null;
        }
        return addresses.stream()
                .map(AddressMapper::toView)
                .collect(Collectors.toList());
    }
}
