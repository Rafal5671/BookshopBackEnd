package com.book.bookshop.service;

import com.book.bookshop.models.Address;
import com.book.bookshop.repo.AddressRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class AddressService {
    @Autowired
    private AddressRepository addressRepository;

    public List<Address> findAll() {
        return addressRepository.findAll();
    }

    public Optional<Address> findById(Integer id) {
        return addressRepository.findById(id);
    }

    public Address save(Address address) {
        return addressRepository.save(address);
    }

    public void deleteById(Integer id) {
        addressRepository.deleteById(id);
    }
}
