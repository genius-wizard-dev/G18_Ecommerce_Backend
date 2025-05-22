package com.vutran0943.basket_service.controllers;

import com.vutran0943.basket_service.client.DiscountClient;
import com.vutran0943.basket_service.client.OrderClient;
import com.vutran0943.basket_service.dto.request.*;
import com.vutran0943.basket_service.dto.response.ApiResponse;
import com.vutran0943.basket_service.dto.response.CartItemResponse;
import com.vutran0943.basket_service.dto.response.CartResponse;
import com.vutran0943.basket_service.dto.response.OrderResponse;
import com.vutran0943.basket_service.entities.Cart;
import com.vutran0943.basket_service.entities.CartItem;
import com.vutran0943.basket_service.enums.CartStatus;
import com.vutran0943.basket_service.exceptions.AppException;
import com.vutran0943.basket_service.exceptions.ErrorCode;
import com.vutran0943.basket_service.services.CartService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RequiredArgsConstructor
@RestController
@RequestMapping("/api/carts")
public class CartController {
    private final CartService cartService;
    private final DiscountClient discountClient;
    private final OrderClient orderClient;

    @PostMapping("/cart-items")
    public ApiResponse<CartItemResponse> addItemToCart(@Valid @RequestBody CartItemCreationRequest cartItemCreationRequest) {
        CartItemResponse response = cartService.addItemToCart(cartItemCreationRequest);

        ApiResponse<CartItemResponse> apiResponse = new ApiResponse<>();
        apiResponse.setMessage("Successfully added item to the cart");
        apiResponse.setData(response);

        return apiResponse;
    }

    @DeleteMapping("/cart-items/{cartItemId}")
    public ApiResponse removeItemFromCart(@PathVariable String cartItemId) {
        cartService.removeItemFromCart(cartItemId);

        ApiResponse apiResponse = new ApiResponse();
        apiResponse.setMessage("Remove the item from the cart successfully");
        apiResponse.setData(null);

        return apiResponse;
    }

    @GetMapping("/users/{userId}")
    public ApiResponse<CartResponse> getCartByUserId(@PathVariable String userId) {
        CartResponse res = cartService.getCartByUserId(userId);

        ApiResponse<CartResponse> apiResponse = new ApiResponse<>();
        apiResponse.setMessage("Successfully retrieved carts");
        apiResponse.setData(res);

        return apiResponse;
    }

    @PatchMapping("/cart-items/{cartItemId}")
    public ApiResponse<CartItem> updateCartItemQuantity(@PathVariable String cartItemId, @Valid @RequestBody UpdateQuantityItemRequest request) {
        CartItem cartItem = cartService.changeItemQuantity(cartItemId, request.getQuantity());

        ApiResponse<CartItem> apiResponse = new ApiResponse<>();
        apiResponse.setMessage("Successfully updated quantity");
        apiResponse.setData(cartItem);

        return apiResponse;
    }

    @PostMapping("/apply-discount")
    @ResponseStatus(HttpStatus.OK)
    public ApiResponse<CartResponse> applyDiscount(@RequestBody ApplyDiscountRequest request)  {
        CartResponse cartResponse = cartService.getCartByCartId(request.getCartId());

        if(cartResponse == null) throw new AppException(ErrorCode.CART_NOT_FOUND);
        List<CartItemResponse> newCartItemList = cartResponse.getCartItems().stream().map(item -> {
            item.setAppliedDiscount(request.getProductIdList().contains(item.getProductId()));
            return item;
        }).toList();

        UserApplyDiscountRequest userApplyDiscountRequest = UserApplyDiscountRequest.builder()
                .cartId(request.getCartId())
                .userId(cartResponse.getUserId())
                .discountId(request.getDiscountId())
                .cartItems(newCartItemList)
                .build();

        ApiResponse<CartResponse> res = discountClient.applyDiscount(userApplyDiscountRequest);

        if(res.getData() != null) cartService.updateCart(res.getData());

        ApiResponse<CartResponse> apiResponse = new ApiResponse<>();
        apiResponse.setCode(res.getCode());
        apiResponse.setMessage(res.getMessage());
        apiResponse.setData(res.getData());

        return apiResponse;
    }

    @PostMapping("/place-order")
    public ApiResponse<OrderResponse> placeOrder(@RequestBody OrderCreationRequest body) {
        CartResponse cartResponse = cartService.getCartByCartId(body.getCartId());

        if(cartResponse.getStatus().equals(CartStatus.INACTIVE.toString())) throw new AppException(ErrorCode.INVALID_CART);

        body.setOrderLineItemList(cartResponse.getCartItems());
        body.setTotalPrice(cartResponse.getTotalPrice());
        ApiResponse<OrderResponse> res = orderClient.placeOrder(body);

        OrderResponse orderResponse = res.getData();
        if(orderResponse != null) cartService.updateCartStatus(body.getCartId());

        return res;
    }

    @DeleteMapping("/carts-items/{cartItemId}")
    public ApiResponse deleteCartItem(@PathVariable String cartItemId) {
        cartService.deleteCartItem(cartItemId);

        return ApiResponse.builder()
                .message("Delete cart item successfully")
                .build();
    }

    @DeleteMapping("/users/{userId}")
    public ApiResponse deleteCart(@PathVariable String userId) {
        cartService.deleteCart(userId);

        return ApiResponse.builder()
                .message("Delete cart successfully")
                .build();
    }
}
