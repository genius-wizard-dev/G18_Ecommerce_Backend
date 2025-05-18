package com.g18.ecommerce.IdentifyService.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;

@Getter
public enum ErrorCode {
    UNCATEGORIZED_EXCEPTION(9999, "Uncategorized error", HttpStatus.INTERNAL_SERVER_ERROR),
    INVALID_KEY(1001, "Uncategorized error", HttpStatus.BAD_REQUEST),
    USER_EXISTED(1002, "User existed", HttpStatus.BAD_REQUEST),
    USERNAME_INVALID(1003, "Username must be at least {min} characters", HttpStatus.BAD_REQUEST),
    INVALID_PASSWORD(1004, "Password must be at least {min} characters", HttpStatus.BAD_REQUEST),
    USER_NOT_EXISTED(1005, "User not existed", HttpStatus.NOT_FOUND),
    UNAUTHENTICATED(1006, "Unauthenticated", HttpStatus.UNAUTHORIZED),
    UNAUTHORIZED(1007, "You do not have permission", HttpStatus.FORBIDDEN),
    INVALID_TOKEN(1008, "Invalid token", HttpStatus.UNAUTHORIZED),
    GENERATE_TOKEN_ERROR(1009, "Generate token error", HttpStatus.INTERNAL_SERVER_ERROR),
    REFRESH_TOKEN_ERROR(1010, "Refresh token error", HttpStatus.INTERNAL_SERVER_ERROR),
    ACCOUNT_LOCKED(1011, "Account locked", HttpStatus.FORBIDDEN),
    ACCOUNT_DELETED(1012, "Account deleted", HttpStatus.FORBIDDEN),
    CREATE_PROFILE_ERROR(1013, "Create profile error", HttpStatus.INTERNAL_SERVER_ERROR),
    CREATE_USER_ERROR(1014, "Create user error", HttpStatus.BAD_REQUEST),
    VALIDATE_ERROR(1015, "Validate error", HttpStatus.BAD_REQUEST),
    ;

    ErrorCode(int code, String message, HttpStatusCode statusCode) {
        this.code = code;
        this.message = message;
        this.statusCode = statusCode;
    }

    private final int code;
    private final String message;
    private final HttpStatusCode statusCode;
}
