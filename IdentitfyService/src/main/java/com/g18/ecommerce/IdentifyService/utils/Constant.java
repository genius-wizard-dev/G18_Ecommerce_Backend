package com.g18.ecommerce.IdentifyService.utils;

public class Constant {
    public static final String ROLE_USER = "USER";
    public static final String ROLE_ADMIN = "ADMIN";
    public static final String ROLE_SALESMAN = "SALESMAN";
    public static final String ROLE_MANAGER = "MANAGER";

    // Status account
    public static final String ACTIVE = "ACTIVE";
    public static final String INACTIVE = "INACTIVE";
    public static final String SUSPENDED = "SUSPENDED";
    public static final String DELETED = "DELETED";

    // Topic for kafka
    public static final String PROFILE_ONBOARDING = "PROFILE_ONBOARDING";
    public static final String PROFILE_ONBOARDED = "PROFILE_ONBOARDED";
    public static final String SUSPEND_PROFILE = "SUSPEND_PROFILE";
    // Validate
    public static final String JSON_REQ_CREATE_USER = "validator/createUser.schema.json";
}
