package com.g18.ecommerce.ProfileService.controllers;

import com.g18.ecommerce.ProfileService.dto.request.AddressCreationRequest;
import com.g18.ecommerce.ProfileService.dto.request.ApiResponse;
import com.g18.ecommerce.ProfileService.dto.response.AddressResponse;
import com.g18.ecommerce.ProfileService.services.AddressService;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/address")
@RequiredArgsConstructor
@FieldDefaults(level = lombok.AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class AddressController {

    AddressService addressService;


    @GetMapping("/get-all/{profileId}")
    public ApiResponse<List<AddressResponse>> getAllAddressByProfileId(@PathVariable String profileId) {
        var authentication = SecurityContextHolder.getContext().getAuthentication();
        log.info("User {} get all address", authentication.getName());
        return ApiResponse.<List<AddressResponse>>builder()
                .result(addressService.getAllAddressByProfileId(profileId))
                .build();
    }

    @PostMapping("/create/{profileId}")
    public ApiResponse<AddressResponse> createAddress(@PathVariable String profileId,
                                                      @RequestBody AddressCreationRequest req) {
        return ApiResponse.<AddressResponse>builder()
                .result(addressService.createAddress(profileId, req))
                .build();
    }

    @PutMapping("/set-default/{profileId}/{addressId}")
    public ApiResponse<Boolean> setDefaultAddress(@PathVariable String profileId, @PathVariable String addressId) {
        return ApiResponse.<Boolean>builder()
                .result(addressService.setDefaultAddress(profileId, addressId))
                .build();
    }

    @PutMapping("/update-type/{addressId}")
    public ApiResponse<AddressResponse> updateAddressType(@PathVariable String addressId,
                                                          @RequestParam(name = "type") String addressType) {
        return ApiResponse.<AddressResponse>builder()
                .result(addressService.updateAddressType(addressId, addressType))
                .build();
    }

}
