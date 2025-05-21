package com.g18.ecommerce.ProfileService.services.servicesimp;

import com.g18.ecommerce.ProfileService.dto.request.AddressCreationRequest;
import com.g18.ecommerce.ProfileService.dto.request.UpdateAddressRequest;
import com.g18.ecommerce.ProfileService.dto.response.AddressResponse;
import com.g18.ecommerce.ProfileService.entity.Address;
import com.g18.ecommerce.ProfileService.entity.AddressType;
import com.g18.ecommerce.ProfileService.exception.AppException;
import com.g18.ecommerce.ProfileService.exception.ErrorCode;
import com.g18.ecommerce.ProfileService.mapper.AddressMapper;
import com.g18.ecommerce.ProfileService.repositories.AddressRepository;
import com.g18.ecommerce.ProfileService.repositories.ProfileRepository;
import com.g18.ecommerce.ProfileService.services.AddressService;
import io.github.resilience4j.retry.annotation.Retry;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PostAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.time.Instant;
import java.util.Date;
import java.util.List;
import java.util.Objects;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = lombok.AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class AddressServiceImpl implements AddressService {


    ProfileRepository profileRepository;
    AddressRepository addressRepository;
    AddressMapper addressMapper;

    @Override
    @Retry(name = "default", fallbackMethod = "fallbackMethod")
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
        for (var address : addresses) {
            if (Objects.equals(req.getWard(), address.getWard()) &&
                    Objects.equals(req.getDistrict(), address.getDistrict()) &&
                    Objects.equals(req.getCity(), address.getCity())) {
                throw new AppException(ErrorCode.ADDRESS_ALREADY_EXISTS);
            }
        }
        return addMoreAddress(profileId, req);
    }

    @Override
    @Retry(name = "default", fallbackMethod = "fallbackMethod")
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
    @Retry(name = "default", fallbackMethod = "fallbackMethod")
    public boolean setDefaultAddress(String profileId, String addressId) {
        var listAddress = getAllAddress(profileId);
        listAddress.forEach(address -> {
            if (address.isDefault()) {
                address.setDefault(false);
                addressRepository.save(address);
            }
        });
        var address = addressRepository.findById(addressId).orElseThrow(() -> new AppException(ErrorCode.ADDRESS_NOT_FOUND));
        address.setDefault(true);
        address = addressRepository.save(address);
        return address.isDefault();
    }

    @Override
    @Retry(name = "default", fallbackMethod = "fallbackMethod")
    public AddressResponse updateAddressType(String addressId, String type) {
        var address = addressRepository.findById(addressId).orElseThrow(() -> new AppException(ErrorCode.ADDRESS_NOT_FOUND));
        address.setType(AddressType.valueOf(type));
        address.setUpdatedAt(new Date(Instant.now().toEpochMilli()));
        address = addressRepository.save(address);
        return addressMapper.toAddressResponse(address);
    }

    @Override
    @Retry(name = "default", fallbackMethod = "fallbackMethod")
    public boolean deleteAddress(String addressId) {
        addressRepository.deleteById(addressId);
        return true;
    }

    @Override
    @Retry(name = "default", fallbackMethod = "fallbackMethod")
    public AddressResponse updateAddress(String addressId, UpdateAddressRequest req) {

        var foundAddress = addressRepository.findById(addressId)
                .orElseThrow(() -> new AppException(ErrorCode.ADDRESS_NOT_FOUND));
        foundAddress.setStreet(req.getStreet());
        foundAddress.setWard(req.getWard());
        foundAddress.setDistrict(req.getDistrict());
        foundAddress.setCity(req.getCity());
        foundAddress.setDetail(req.getDetail());
        foundAddress.setPhoneShip(req.getPhoneShip());
        foundAddress.setUpdatedAt(new Date(Instant.now().toEpochMilli()));
        var response = addressRepository.save(foundAddress);
        return addressMapper.toAddressResponse(response);
    }
    public String fallbackMethod(Exception ex) {
        return "Hệ thống hiện đang bận, vui lòng thử lại sau.";
    }
}
