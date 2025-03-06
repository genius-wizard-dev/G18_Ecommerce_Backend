package com.g18.ecommerce.ProfileService.services;


import com.g18.ecommerce.ProfileService.dto.request.ProfileCreationRequest;
import com.g18.ecommerce.ProfileService.dto.request.UpdateProfileRequest;
import com.g18.ecommerce.ProfileService.dto.response.ProfileResponse;

public interface ProfileService {
    public String createProfile(String request);
    public ProfileResponse updateProfile(String id,UpdateProfileRequest request);
    public void inactiveProfile(String userId);
    public ProfileResponse registerShop(String profileId);

}
