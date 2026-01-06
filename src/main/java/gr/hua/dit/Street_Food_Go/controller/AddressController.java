package gr.hua.dit.Street_Food_Go.controller;

import gr.hua.dit.Street_Food_Go.dto.AddressView;
import gr.hua.dit.Street_Food_Go.model.Address;
import gr.hua.dit.Street_Food_Go.service.AddressService;
import gr.hua.dit.Street_Food_Go.util.AddressMapper;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/addresses")
public class AddressController {

    private final AddressService addressService;

    public AddressController(final AddressService addressService) {
        if (addressService == null) {
            throw new NullPointerException("addressService cannot be null");
        }
        this.addressService = addressService;
    }

    @PostMapping("/user/{userId}")
    public ResponseEntity<AddressView> createAddress(@PathVariable Long userId, @RequestBody Address address) {
        try {
            Address createdAddress = addressService.createAddress(userId, address);
            return new ResponseEntity<>(AddressMapper.toView(createdAddress), HttpStatus.CREATED);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<List<AddressView>> getAddressesByUserId(@PathVariable Long userId) {
        List<Address> addresses = addressService.getAddressesByUserId(userId);
        return ResponseEntity.ok(AddressMapper.toViewList(addresses));
    }

    @GetMapping("/{id}")
    public ResponseEntity<AddressView> getAddressById(@PathVariable Long id) {
        return addressService.getAddressById(id)
                .map(address -> ResponseEntity.ok(AddressMapper.toView(address)))
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/user/{userId}/default")
    public ResponseEntity<AddressView> getDefaultAddress(@PathVariable Long userId) {
        return addressService.getDefaultAddress(userId)
                .map(address -> ResponseEntity.ok(AddressMapper.toView(address)))
                .orElse(ResponseEntity.notFound().build());
    }

    @PutMapping("/{id}")
    public ResponseEntity<AddressView> updateAddress(@PathVariable Long id, @RequestBody Address address) {
        try {
            Address updatedAddress = addressService.updateAddress(id, address);
            return ResponseEntity.ok(AddressMapper.toView(updatedAddress));
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @PatchMapping("/{id}/set-default")
    public ResponseEntity<AddressView> setAsDefault(@PathVariable Long id) {
        try {
            Address address = addressService.setAsDefault(id);
            return ResponseEntity.ok(AddressMapper.toView(address));
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteAddress(@PathVariable Long id) {
        try {
            addressService.deleteAddress(id);
            return ResponseEntity.noContent().build();
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }
}
