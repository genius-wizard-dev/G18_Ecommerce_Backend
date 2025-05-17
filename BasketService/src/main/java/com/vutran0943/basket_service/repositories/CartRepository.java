package com.vutran0943.basket_service.repositories;

import com.vutran0943.basket_service.entities.Cart;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface CartRepository extends JpaRepository<Cart, String> {
    public Cart getCartByUserIdAndStatus(String userId, String status);
    public Cart getCartById(String cartId);
    @Modifying
    @Query("update Cart cart set cart.status = :status where cart.id = :cartId")
    public void updateCartStatus(@Param("cartId") String cartId, @Param("status") String status);
}
