package com.buyology.backend.Controller;

import com.buyology.backend.dto.AddressDTO;
import com.buyology.backend.dto.CategoryDTO;
import com.buyology.backend.model.User;
import com.buyology.backend.service.AddressService;
import com.buyology.backend.utils.AuthUtil;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
public class AddressController {

    private final AddressService addressService;
    private final Logger log = LoggerFactory.getLogger(AddressController.class);
    private final AuthUtil authUtil;

    public AddressController(AddressService addressService, AuthUtil authUtil){
        this.addressService = addressService;
        this.authUtil = authUtil;
    }

    @PostMapping("/addresses")
    public ResponseEntity<AddressDTO> createAddress(@RequestBody AddressDTO addressDTO){
        log.info("Requested to add a new Address");
        User loggedInUser = authUtil.loggedInUser();
        return ResponseEntity.status(HttpStatus.CREATED).body(addressService.createAddress(addressDTO, loggedInUser));
    }

    @GetMapping("/addresses")
    public ResponseEntity<List<AddressDTO>> getAddress(){
        log.info("Requested to get all the addresses");
        return ResponseEntity.status(HttpStatus.OK).body(addressService.getAddresses());
    }

    @GetMapping("/addresses/{addressId}")
    public ResponseEntity<AddressDTO> getAddress(@PathVariable Long addressId){
        log.info("Requested to get the address with id {}",addressId);
        return ResponseEntity.status(HttpStatus.OK).body(addressService.getAddressById(addressId));
    }

    @GetMapping("/user/addresses")
    public ResponseEntity<List<AddressDTO>> getUserAddresses(){
        log.info("Requested to get the address for logged in user");
        return ResponseEntity.status(HttpStatus.OK).body(addressService.getUserAddress(authUtil.loggedInUser()));
    }

    @PatchMapping("/addresses/{addressId}")
    public ResponseEntity<AddressDTO> updateAddressById(@PathVariable Long addressId, @RequestBody AddressDTO addressDTO){
        log.info("Requested to update the address with id {}",addressId);
        return ResponseEntity.status(HttpStatus.OK).body(addressService.updateAddressById(addressId,addressDTO));
    }

    @DeleteMapping("/addresses/{addressId}")
    public ResponseEntity<String> deleteAddressById(@PathVariable Long addressId){
        log.info("Requested to delete the address with id {}",addressId);
        return ResponseEntity.status(HttpStatus.OK).body(addressService.deleteAddressById(addressId));
    }

}
