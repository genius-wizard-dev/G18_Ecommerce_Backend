package com.g18.ecommerce.IdentifyService.repositories;

import com.g18.ecommerce.IdentifyService.entity.InvalidatedToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface InvalidatedRepository extends JpaRepository<InvalidatedToken, String> {
}
