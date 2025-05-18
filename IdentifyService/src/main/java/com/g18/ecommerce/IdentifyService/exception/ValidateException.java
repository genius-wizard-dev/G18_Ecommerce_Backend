package com.g18.ecommerce.IdentifyService.exception;

import lombok.Getter;
import lombok.experimental.FieldDefaults;
import org.springframework.http.HttpStatus;

import java.util.Map;

@Getter
@FieldDefaults(makeFinal = true, level = lombok.AccessLevel.PRIVATE)
public class ValidateException extends RuntimeException {

    ErrorCode errorCode;

    Map<String, String> errors;


    public ValidateException(ErrorCode code, Map<String, String> errors) {
        this.errorCode = code;
        this.errors = errors;
    }
}
