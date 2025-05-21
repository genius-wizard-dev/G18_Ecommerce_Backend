package com.g18.ecommerce.ProfileService.repositories;

import com.g18.ecommerce.ProfileService.entity.Profile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProfileRepository extends JpaRepository<Profile, String> {
    Optional<Profile> findByUserId(String userId);
    Optional<Profile> findByShopId(String shopId);
    List<Profile> findByShopNameContaining(String keyword);

    List<Profile> findByShopIdIsNotNull();
}
