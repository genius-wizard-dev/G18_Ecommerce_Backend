package com.g18.ecommerce.ProfileService.entity;

import lombok.Getter;

@Getter
public enum AddressType {
    HOME("HOME"),
    WORK("WORK"),
    OTHER("OTHER");


    private AddressType(String type) {
        this.type = type;
    }

    private final String type;
}
