package com.g18.ecommerce.ProfileService.controllers;

import com.g18.ecommerce.ProfileService.dto.request.ApiResponse;
import com.g18.ecommerce.ProfileService.dto.request.UpdateProfileRequest;
import com.g18.ecommerce.ProfileService.dto.response.ProfileResponse;
import com.g18.ecommerce.ProfileService.services.ProfileService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@FieldDefaults(level = lombok.AccessLevel.PRIVATE, makeFinal = true)
public class ProfileController {
    ProfileService profileService;
    @PutMapping("/{id}")
    public ApiResponse<ProfileResponse> updateProfile(@RequestBody @Valid UpdateProfileRequest req, @PathVariable String id ) {
        return ApiResponse.<ProfileResponse>builder()
                .result(profileService.updateProfile(id,req))
                .build();
    }

    @PutMapping("/register-shop/{profileId}")
    public ApiResponse<ProfileResponse> registerShop(@PathVariable String profileId) {
        return ApiResponse.<ProfileResponse>builder()
                .result(profileService.registerShop(profileId))
                .build();
    }

}
