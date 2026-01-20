package gr.hua.dit.Street_Food_Go.service.impl;

import gr.hua.dit.Street_Food_Go.model.Address;
import gr.hua.dit.Street_Food_Go.model.User;
import gr.hua.dit.Street_Food_Go.repository.AddressRepository;
import gr.hua.dit.Street_Food_Go.repository.UserRepository;
import gr.hua.dit.Street_Food_Go.service.AddressService;
import gr.hua.dit.Street_Food_Go.service.external.GeocodingResult;
import gr.hua.dit.Street_Food_Go.service.external.GeocodingService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class AddressServiceImpl implements AddressService {

    private static final Logger logger = LoggerFactory.getLogger(AddressServiceImpl.class);

    private final AddressRepository addressRepository;
    private final UserRepository userRepository;
    private final GeocodingService geocodingService;

    public AddressServiceImpl(
            final AddressRepository addressRepository,
            final UserRepository userRepository,
            final GeocodingService geocodingService) {
        if (addressRepository == null) {
            throw new NullPointerException("addressRepository cannot be null");
        }
        if (userRepository == null) {
            throw new NullPointerException("userRepository cannot be null");
        }
        if (geocodingService == null) {
            throw new NullPointerException("geocodingService cannot be null");
        }
        this.addressRepository = addressRepository;
        this.userRepository = userRepository;
        this.geocodingService = geocodingService;
    }

    /**
     * Geocode the address if coordinates are not provided.
     */
    private void geocodeIfNeeded(Address address) {
        if (address.getLatitude() == null || address.getLongitude() == null) {
            logger.info("Geocoding address: {}, {}", address.getStreetAddress(), address.getCity());

            GeocodingResult result = geocodingService.geocodeAddress(
                    address.getStreetAddress(),
                    address.getCity(),
                    address.getPostalCode(),
                    "Greece"  // Default to Greece
            );

            if (result.isSuccess()) {
                address.setLatitude(result.getLatitude());
                address.setLongitude(result.getLongitude());
                logger.info("Geocoded to lat={}, lon={}", result.getLatitude(), result.getLongitude());
            } else {
                logger.warn("Geocoding failed: {}", result.getErrorMessage());
            }
        }
    }

    @Override
    public Address createAddress(Long userId, Address address) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found with id: " + userId));
        address.setUser(user);

        // Auto-geocode if coordinates not provided
        geocodeIfNeeded(address);

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

        // Auto-geocode if coordinates not provided
        geocodeIfNeeded(address);

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
