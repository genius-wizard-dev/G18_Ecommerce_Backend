package com.vutran0943.basket_service.services;

import com.vutran0943.basket_service.dto.request.CartItemCreationRequest;
import com.vutran0943.basket_service.dto.response.CartItemResponse;
import com.vutran0943.basket_service.dto.response.CartResponse;
import com.vutran0943.basket_service.entities.Cart;
import com.vutran0943.basket_service.entities.CartItem;
import com.vutran0943.basket_service.exceptions.AppException;
import com.vutran0943.basket_service.exceptions.ErrorCode;
import com.vutran0943.basket_service.mappers.CartItemMapper;
import com.vutran0943.basket_service.mappers.CartMapper;
import com.vutran0943.basket_service.repositories.CartItemRepository;
import com.vutran0943.basket_service.repositories.CartRepository;
import com.vutran0943.basket_service.enums.CartStatus;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@RequiredArgsConstructor
@Service
public class CartService {
    private final CartRepository cartRepository;
    private final CartItemRepository cartItemRepository;
    private final CartMapper cartMapper;
    private final CartItemMapper cartItemMapper;

    private Cart createCart (String userId) {
        Cart cart = cartRepository.getCartByUserIdAndStatus(userId, CartStatus.ACTIVE.toString());

        if(cart != null) return cart;

        cart = Cart.builder()
                .userId(userId)
                .status(CartStatus.ACTIVE.toString())
                .build();

        return cartRepository.save(cart);
    }

    public CartItemResponse addItemToCart(CartItemCreationRequest request) {
        Cart cart = createCart(request.getUserId());
        CartItem cartItem = cartItemRepository.getCartItemByCartIdAndProductId(cart.getId(), request.getProductId());

        if(cartItem == null) {
            cartItem  = cartItemMapper.toCartItem(request);
            cartItem.setCart(cart);
            cartItem.setFinalPrice(request.getPrice());
        } else {
            cartItem.setQuantity(cartItem.getQuantity() + request.getQuantity());
            cartItem.setPrice(request.getPrice());
        }

        CartItem newCartItem = cartItemRepository.save(cartItem);
        if(cart.getCartItems() == null) {
            cart.setTotalPrice(newCartItem.getPrice()*newCartItem.getQuantity());
            cartRepository.save(cart);
        } else calculateTotalPrice(cart.getId());

        return cartItemMapper.toCartItemResponse(newCartItem);
    }

    public void removeItemFromCart(String cartItemId) {
        cartItemRepository.deleteById(cartItemId);
    }

    public CartResponse getCartByUserId(String userId) {
        Cart cart =  cartRepository.getCartByUserIdAndStatus(userId, CartStatus.ACTIVE.toString());
        System.out.println(cart);
        return cartMapper.toCartResponse(cart);
    }

    public CartResponse getCartByCartId(String cartId) {
        return cartMapper.toCartResponse(cartRepository.getCartById(cartId));
    }

    public void updateCart(CartResponse cartResponse) {
        Cart newCart = cartMapper.toCart(cartResponse);

        for(CartItem cartItem : newCart.getCartItems()) {
            cartItem.setCart(newCart);
        }

        newCart.setStatus(CartStatus.ACTIVE.toString());
        cartRepository.save(newCart);
    }

    @Transactional
    public void updateCartStatus(String cartId) {
        cartRepository.updateCartStatus(cartId, CartStatus.INACTIVE.toString());
    }

    public void deleteCartItem(String cartItemId) {
        cartItemRepository.deleteById(cartItemId);
    }

    @Transactional
    public void deleteCart(String userId) {
        Cart cart = cartRepository.getCartByUserIdAndStatus(userId, CartStatus.ACTIVE.toString());
        cartItemRepository.deleteByCart(cart);
    }

    public CartItem changeItemQuantity(String cartItemId, int quantity) {
        CartItem cartItem = cartItemRepository.getCartItemById(cartItemId);
        if(cartItem == null) throw new AppException(ErrorCode.CART_ITEM_NOT_FOUND);
        cartItem.setQuantity(quantity);

        CartItem newCartItem =  cartItemRepository.save(cartItem);
        calculateTotalPrice(cartItem.getCart().getId());
        return newCartItem;
    }

    private void calculateTotalPrice(String cartId) {
        Cart cart = cartRepository.getCartById(cartId);
        System.out.println(cart);
        double totalPrice = 0;

        for(CartItem cartItem : cart.getCartItems()) {
            totalPrice += cartItem.getFinalPrice()*cartItem.getQuantity();
        }

        cart.setTotalPrice(totalPrice);

        cartRepository.save(cart);
    }
}
