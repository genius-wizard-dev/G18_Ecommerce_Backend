package com.g18.ecommerce.IdentifyService.entity;

import lombok.Getter;

@Getter
public enum Status {
    ACTIVE("ACTIVE"), INACTIVE("INACTIVE"), SUSPENDED("SUSPENDED"), DELETED("DELETED");

    Status(String name){
        this.name = name;
    }

    private final String name;

    
}
