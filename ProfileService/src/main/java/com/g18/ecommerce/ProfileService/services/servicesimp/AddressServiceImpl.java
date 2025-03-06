package com.g18.ecommerce.ProfileService.services.servicesimp;

import com.g18.ecommerce.ProfileService.dto.request.AddressCreationRequest;
import com.g18.ecommerce.ProfileService.dto.response.AddressResponse;
import com.g18.ecommerce.ProfileService.entity.Address;
import com.g18.ecommerce.ProfileService.entity.AddressType;
import com.g18.ecommerce.ProfileService.exception.AppException;
import com.g18.ecommerce.ProfileService.exception.ErrorCode;
import com.g18.ecommerce.ProfileService.mapper.AddressMapper;
import com.g18.ecommerce.ProfileService.repositories.AddressRepository;
import com.g18.ecommerce.ProfileService.repositories.ProfileRepository;
import com.g18.ecommerce.ProfileService.services.AddressService;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PostAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.time.Instant;
import java.util.Date;
import java.util.List;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = lombok.AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class AddressServiceImpl implements AddressService {


    ProfileRepository profileRepository;
    AddressRepository addressRepository;
    AddressMapper addressMapper;

    @Override
    public AddressResponse createAddress(String profileId, AddressCreationRequest req) {
        var addresses = addressRepository.getAllByProfileId(profileId);
        if (CollectionUtils.isEmpty(addresses)) {
            Address address = addressMapper.toAddress(req);
            address.setProfile(profileRepository.findById(profileId).orElse(null));
            address.setDefault(true);
            address.setCreatedAt(new Date(Instant.now().toEpochMilli()));
            address.setUpdatedAt(new Date(Instant.now().toEpochMilli()));
            address = addressRepository.save(address);
            var response = addressMapper.toAddressResponse(address);
            response.setProfileId(profileId);
            response.setDefault(address.isDefault());
            return response;
        }
        return addMoreAddress(profileId, req);
    }

    @Override
    public List<AddressResponse> getAllAddressByProfileId(String profileId) {
        return getAllAddress(profileId).stream()
                .map(obj -> {
                    var response = addressMapper.toAddressResponse(obj);
                    response.setProfileId(profileId);
                    response.setDefault(obj.isDefault());
                    return response;
                })
                .toList();
    }

    private AddressResponse addMoreAddress(String profileId, AddressCreationRequest req) {
        Address address = addressMapper.toAddress(req);
        address.setProfile(profileRepository.findById(profileId).orElse(null));
        address.setDefault(false);
        address.setCreatedAt(new Date(Instant.now().toEpochMilli()));
        address.setUpdatedAt(new Date(Instant.now().toEpochMilli()));
        address = addressRepository.save(address);
        var response = addressMapper.toAddressResponse(address);
        response.setProfileId(profileId);
        response.setDefault(address.isDefault());
        return response;
    }
    private List<Address> getAllAddress(String profileId) {
        return addressRepository.getAllByProfileId(profileId);
    }
    @Override
    public boolean setDefaultAddress(String profileId, String addressId) {
        var listAddress = getAllAddress(profileId);
        listAddress.forEach(address ->{
            if(address.isDefault()){
                address.setDefault(false);
                addressRepository.save(address);
            }
        });
        var address = addressRepository.findById(addressId).orElseThrow(()-> new AppException(ErrorCode.ADDRESS_NOT_FOUND));
        address.setDefault(true);
        address = addressRepository.save(address);
        return address.isDefault();
    }

    @Override
    public AddressResponse updateAddressType(String addressId, String type) {
        var address = addressRepository.findById(addressId).orElseThrow(()-> new AppException(ErrorCode.ADDRESS_NOT_FOUND));
        address.setType(AddressType.valueOf(type));
        address.setUpdatedAt(new Date(Instant.now().toEpochMilli()));
        address = addressRepository.save(address);
        return addressMapper.toAddressResponse(address);
    }
}
