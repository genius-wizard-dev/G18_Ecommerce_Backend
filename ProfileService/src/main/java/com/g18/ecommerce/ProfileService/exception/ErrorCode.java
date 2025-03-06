package com.g18.ecommerce.ProfileService.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;

@Getter
public enum ErrorCode {
    UNCATEGORIZED_EXCEPTION(9999, "Uncategorized error", HttpStatus.INTERNAL_SERVER_ERROR),
    INVALID_KEY(1001, "Uncategorized error", HttpStatus.BAD_REQUEST),
    UNAUTHENTICATED(1006, "Unauthenticated", HttpStatus.UNAUTHORIZED),
    UNAUTHORIZED(1007, "You do not have permission", HttpStatus.FORBIDDEN),
    INVALID_TOKEN(1008, "Invalid token", HttpStatus.UNAUTHORIZED),
    GENERATE_TOKEN_ERROR(1009, "Generate token error", HttpStatus.INTERNAL_SERVER_ERROR),
    REFRESH_TOKEN_ERROR(1010, "Refresh token error", HttpStatus.INTERNAL_SERVER_ERROR),
    ACCOUNT_LOCKED(1011, "Account locked", HttpStatus.FORBIDDEN),
    PROFILE_NOT_FOUND(1012, "Profile not found", HttpStatus.NOT_FOUND),
    INVALID_DOB(1013, "Your age must be at least {min}", HttpStatus.BAD_REQUEST),
    INVALID_DISPLAY_NAME(1014, "Display name must be at least {min} characters", HttpStatus.BAD_REQUEST),
    INVALID_AVATAR_URL(1015, "Avatar is not a valid URL", HttpStatus.BAD_REQUEST),
    ADDRESS_NOT_FOUND(1016, "Address not found", HttpStatus.NOT_FOUND),
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
