package com.g18.ecommerce.ProfileService.services;


import com.g18.ecommerce.ProfileService.dto.request.ProfileCreationRequest;
import com.g18.ecommerce.ProfileService.dto.request.RegisterShopRequest;
import com.g18.ecommerce.ProfileService.dto.request.UpdateProfileRequest;
import com.g18.ecommerce.ProfileService.dto.response.ProfileResponse;

import java.util.List;

public interface ProfileService {
    public String createProfile(String request);
    public ProfileResponse updateProfile(String id,UpdateProfileRequest request);
    public void inactiveProfile(String userId);
    public ProfileResponse registerShop(String profileId, RegisterShopRequest req);
    public ProfileResponse getProfileByUserId(String userId );
    public boolean checkIsShop(String userId);
    public ProfileResponse getUserByShopId(String shopId);
    public List<ProfileResponse> findShopsByName(String name);
    public List<ProfileResponse> getAllShop();
}
