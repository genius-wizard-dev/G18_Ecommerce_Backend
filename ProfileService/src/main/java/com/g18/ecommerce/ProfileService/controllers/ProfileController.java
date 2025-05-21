package com.g18.ecommerce.ProfileService.controllers;

import com.g18.ecommerce.ProfileService.dto.request.ApiResponse;
import com.g18.ecommerce.ProfileService.dto.request.RegisterShopRequest;
import com.g18.ecommerce.ProfileService.dto.request.UpdateProfileRequest;
import com.g18.ecommerce.ProfileService.dto.response.ProfileResponse;
import com.g18.ecommerce.ProfileService.services.ProfileService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.apache.kafka.shaded.com.google.protobuf.Api;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@FieldDefaults(level = lombok.AccessLevel.PRIVATE, makeFinal = true)
public class ProfileController {
    ProfileService profileService;
    @GetMapping("/{id}")
    public ApiResponse<ProfileResponse> getProfile(@PathVariable String id){
        return ApiResponse.<ProfileResponse>builder()
                .result(profileService.getProfileByUserId(id))
                .build();
    }

    @PutMapping("/{id}")
    public ApiResponse<ProfileResponse> updateProfile(@RequestBody @Valid UpdateProfileRequest req, @PathVariable String id ) {
        return ApiResponse.<ProfileResponse>builder()
                .result(profileService.updateProfile(id,req))
                .build();
    }
    @PutMapping("/register-shop/{profileId}")
    public ApiResponse<ProfileResponse> registerShop(@PathVariable String profileId, @RequestBody @Valid RegisterShopRequest req) {
        return ApiResponse.<ProfileResponse>builder()
                .result(profileService.registerShop(profileId, req))
                .build();
    }

    @GetMapping("/check-shop/{userId}")
    public ApiResponse<Boolean> checkIsShop(@PathVariable String userId) {
        return ApiResponse.<Boolean>builder()
                .result(profileService.checkIsShop(userId))
                .build();
    }

    @GetMapping("/shop/{shopId}")
    public ApiResponse<ProfileResponse> getShopById( @PathVariable String shopId){
        return ApiResponse.<ProfileResponse>builder()
                .result(profileService.getUserByShopId(shopId))
                .build();
    }

    @GetMapping("/shop/search/{name}")
    public ApiResponse<List<ProfileResponse>> getListShopByName(@PathVariable String name){
        return ApiResponse.<List<ProfileResponse>>builder()
                .result(profileService.findShopsByName(name))
                .build();
    }



}
