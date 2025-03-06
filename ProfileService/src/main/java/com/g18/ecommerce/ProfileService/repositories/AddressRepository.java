package com.g18.ecommerce.ProfileService.repositories;

import com.g18.ecommerce.ProfileService.entity.Address;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AddressRepository extends JpaRepository<Address, String> {
    List<Address> getAllByProfileId(String profileId);
}
