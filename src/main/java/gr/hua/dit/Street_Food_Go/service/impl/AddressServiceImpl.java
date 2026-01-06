package gr.hua.dit.Street_Food_Go.service.impl;

import gr.hua.dit.Street_Food_Go.model.Address;
import gr.hua.dit.Street_Food_Go.model.User;
import gr.hua.dit.Street_Food_Go.repository.AddressRepository;
import gr.hua.dit.Street_Food_Go.repository.UserRepository;
import gr.hua.dit.Street_Food_Go.service.AddressService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class AddressServiceImpl implements AddressService {

    private final AddressRepository addressRepository;
    private final UserRepository userRepository;

    public AddressServiceImpl(final AddressRepository addressRepository, final UserRepository userRepository) {
        if (addressRepository == null) {
            throw new NullPointerException("addressRepository cannot be null");
        }
        if (userRepository == null) {
            throw new NullPointerException("userRepository cannot be null");
        }
        this.addressRepository = addressRepository;
        this.userRepository = userRepository;
    }

    @Override
    public Address createAddress(Long userId, Address address) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found with id: " + userId));
        address.setUser(user);
        return addressRepository.save(address);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Address> getAddressesByUserId(Long userId) {
        return addressRepository.findByUserId(userId);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Address> getAddressById(Long id) {
        return addressRepository.findById(id);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Address> getDefaultAddress(Long userId) {
        return addressRepository.findByUserIdAndIsDefaultTrue(userId);
    }

    @Override
    public Address updateAddress(Long id, Address addressDetails) {
        Address address = addressRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Address not found with id: " + id));

        address.setStreetAddress(addressDetails.getStreetAddress());
        address.setCity(addressDetails.getCity());
        address.setPostalCode(addressDetails.getPostalCode());
        address.setLatitude(addressDetails.getLatitude());
        address.setLongitude(addressDetails.getLongitude());

        return addressRepository.save(address);
    }

    @Override
    public Address setAsDefault(Long addressId) {
        Address address = addressRepository.findById(addressId)
                .orElseThrow(() -> new RuntimeException("Address not found with id: " + addressId));

        // Remove default from other addresses of the same user
        List<Address> userAddresses = addressRepository.findByUserId(address.getUser().getId());
        for (Address addr : userAddresses) {
            if (addr.isDefault()) {
                addr.setDefault(false);
                addressRepository.save(addr);
            }
        }

        address.setDefault(true);
        return addressRepository.save(address);
    }

    @Override
    public void deleteAddress(Long id) {
        addressRepository.deleteById(id);
    }
}
