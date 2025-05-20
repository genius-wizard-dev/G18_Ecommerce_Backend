package com.vutran0943.basket_service.exceptions;

import lombok.Getter;

@Getter
public enum ErrorCode {
    CART_ITEM_NOT_FOUND(5001, 400, "Cart item does not exist"),
    CART_NOT_FOUND(5002, 400, "Cart does not exist"),
    INVALID_CART(5003, 400, "Invalid cart"),
    UNCATEGORIZED_ERROR(9999, 500, "Uncategorized Error");

    private int code;
    private int status;
    private String message;

    ErrorCode(int code, int status, String message) {
        this.code = code;
        this.status = status;
        this.message = message;
    }
}
