package com.g18.ecommerce.ProfileService.mapper;

import com.g18.ecommerce.ProfileService.dto.request.AddressCreationRequest;
import com.g18.ecommerce.ProfileService.dto.response.AddressResponse;
import com.g18.ecommerce.ProfileService.entity.Address;
import com.g18.ecommerce.ProfileService.entity.AddressType;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface AddressMapper {
    @Mapping(target="type", expression = "java(convertToAddressType(req.getType()))")
    Address toAddress(AddressCreationRequest req);

    @Mapping(target="type", expression = "java(convertToAddressType(address.getType()))")
    @Mapping(target = "profileId", ignore = true)
    @Mapping(target="isDefault", ignore = true)
    AddressResponse toAddressResponse(Address address);

    default AddressType convertToAddressType(String type) {
        if (type == null) {
            return null;
        }
        try {
            return AddressType.valueOf(type);
        } catch (IllegalArgumentException e) {
            return null;
        }
    }
    default String convertToAddressType(AddressType type) {
        if (type == null) {
            return null;
        }
        return type.name();
    }
}
