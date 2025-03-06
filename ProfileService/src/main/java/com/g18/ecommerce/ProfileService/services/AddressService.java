package com.g18.ecommerce.ProfileService.services;

import com.g18.ecommerce.ProfileService.dto.request.AddressCreationRequest;
import com.g18.ecommerce.ProfileService.dto.response.AddressResponse;

import java.util.List;

public interface AddressService {
    public AddressResponse createAddress(String profileId,AddressCreationRequest req);
    public List<AddressResponse> getAllAddressByProfileId(String profileId);
    public boolean setDefaultAddress(String profileId, String addressId);
    public AddressResponse updateAddressType(String addressId, String type);
}
