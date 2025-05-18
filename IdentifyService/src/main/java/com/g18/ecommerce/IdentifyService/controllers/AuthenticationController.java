package com.g18.ecommerce.IdentifyService.controllers;

import com.g18.ecommerce.IdentifyService.dto.request.*;
import com.g18.ecommerce.IdentifyService.dto.response.AuthenticationResponse;
import com.g18.ecommerce.IdentifyService.dto.response.IntrospectResponse;
import com.g18.ecommerce.IdentifyService.services.AuthenticationService;
import com.nimbusds.jose.JOSEException;
import jakarta.mail.MessagingException;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
@FieldDefaults(level = lombok.AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class AuthenticationController {
    AuthenticationService authenticationService;
    @PostMapping("/log-in")
    ApiResponse<AuthenticationResponse> authenticate(@RequestBody AuthenticationRequest request) {
        return ApiResponse.<AuthenticationResponse>builder()
                .result(authenticationService.authenticate(request))
                .build();
    }

    @PostMapping("/refresh")
    ApiResponse<AuthenticationResponse> refresh(@RequestBody RefreshRequest request) throws JOSEException {
        return ApiResponse.<AuthenticationResponse>builder()
                .result(authenticationService.refreshToken(request))
                .build();
    }

    @PostMapping("/introspect")
    ApiResponse<IntrospectResponse> authenticate(@RequestBody IntrospectRequest request) {
        var result = authenticationService.introspect(request);
        return ApiResponse.<IntrospectResponse>builder().result(result).build();
    }

    @PostMapping("/logout")
    ApiResponse<Void> logout(@RequestBody LogoutRequest req){
        authenticationService.logout(req);
        return ApiResponse.<Void>builder()
                .build();
    }

    @PostMapping("/send-otp-change-password/{userId}")
    public ApiResponse<?> sendOtp(@PathVariable String userId, @RequestBody OtpRequest request) throws MessagingException {
        return ApiResponse.builder()
                .result(authenticationService.sendOtp(userId, request))
                .build();
    }

    @PostMapping("/change-password/{userId}")
    public ApiResponse<Boolean> changePassword(@PathVariable String userId, @RequestBody ChangePasswordRequest request) {
        boolean result = authenticationService.changePassword(userId, request);
            return ApiResponse.<Boolean>builder()
                    .result(result)
                    .build();

    }

}
