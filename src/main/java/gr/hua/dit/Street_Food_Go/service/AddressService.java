package gr.hua.dit.Street_Food_Go.service;

import gr.hua.dit.Street_Food_Go.model.Address;

import java.util.List;
import java.util.Optional;

/**
 * Service interface for managing {@code Address} entities.
 */
public interface AddressService {

    Address createAddress(Long userId, Address address);

    List<Address> getAddressesByUserId(Long userId);

    Optional<Address> getAddressById(Long id);

    Optional<Address> getDefaultAddress(Long userId);

    Address updateAddress(Long id, Address addressDetails);

    Address setAsDefault(Long addressId);

    void deleteAddress(Long id);
}
