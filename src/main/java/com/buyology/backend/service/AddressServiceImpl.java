package com.buyology.backend.service;

import com.buyology.backend.dto.AddressDTO;
import com.buyology.backend.exception.ResourceNotFoundException;
import com.buyology.backend.model.Address;
import com.buyology.backend.model.User;
import com.buyology.backend.repository.AddressRepository;
import jakarta.transaction.Transactional;
import org.modelmapper.ModelMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class AddressServiceImpl implements AddressService {

    private final AddressRepository addressRepository;
    private final ModelMapper modelMapper;

    private final Logger log = LoggerFactory.getLogger(AddressServiceImpl.class);

    public AddressServiceImpl(AddressRepository addressRepository, ModelMapper modelMapper) {
        this.addressRepository = addressRepository;
        this.modelMapper = modelMapper;
    }

    @Override
    public AddressDTO createAddress(AddressDTO addressDTO, User user) {
        log.info("Creating address for userId={}", user.getUserId());

        Address address = modelMapper.map(addressDTO, Address.class);
        user.addAddress(address);

        Address savedAddress = addressRepository.save(address);

        log.info("Address saved for userId={}", user.getUserId());
        return modelMapper.map(savedAddress, AddressDTO.class);
    }

    @Override
    public List<AddressDTO> getAddresses() {

        List<Address> addresses = addressRepository.findAll();
        log.info("Returned {} Addresses", addresses.size());
        return addresses.stream()
                .map(ad -> modelMapper.map(ad, AddressDTO.class))
                .toList();
    }

    @Override
    public AddressDTO getAddressById(Long addressId) {
        Address addressById = addressRepository.findById(addressId)
                .orElseThrow(() -> new ResourceNotFoundException("Address", "addressId", addressId));
        return modelMapper.map(addressById, AddressDTO.class);
    }

    @Override
    public List<AddressDTO> getUserAddress(User user) {
        List<Address> addresses = user.getAddresses();
        log.info("Returned {} Addresses for this particular user with id {}", addresses.size(), user.getUserId());
        return addresses.stream()
                .map(ad -> modelMapper.map(ad, AddressDTO.class))
                .toList();
    }

    @Override
    public AddressDTO updateAddressById(Long addressId, AddressDTO addressDTO) {
        Address address = addressRepository.findById(addressId)
                .orElseThrow(() -> new ResourceNotFoundException("Address", "addressId", addressId));

        if (addressDTO.getStreet() != null) {
            address.setStreet(addressDTO.getStreet());
        }
        if (addressDTO.getBuildingName() != null) {
            address.setBuildingName(addressDTO.getBuildingName());
        }
        if (addressDTO.getCity() != null) {
            address.setCity(addressDTO.getCity());
        }
        if (addressDTO.getState() != null) {
            address.setState(addressDTO.getState());
        }
        if (addressDTO.getCountry() != null) {
            address.setCountry(addressDTO.getCountry());
        }
        if (addressDTO.getZipCode() != null) {
            address.setZipCode(addressDTO.getZipCode());
        }
        Address savedAddress = addressRepository.save(address);
        return modelMapper.map(savedAddress, AddressDTO.class);
    }

    @Override
    @Transactional
    public String deleteAddressById(Long addressId) {
        Address address = addressRepository.findById(addressId)
                .orElseThrow(() -> new ResourceNotFoundException("Address", "addressId", addressId));

        List<User> users = new ArrayList<>(address.getUsers());

        for (User user : users) {
            user.removeAddress(address);
        }
        addressRepository.delete(address);
        return "Address deleted successfully with id: " + addressId;
    }
}
