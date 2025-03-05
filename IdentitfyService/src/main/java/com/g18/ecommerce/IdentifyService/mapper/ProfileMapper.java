package com.g18.ecommerce.IdentifyService.mapper;

import com.g18.ecommerce.IdentifyService.dto.request.ProfileCreationRequest;
import com.g18.ecommerce.IdentifyService.dto.request.UserCreationRequest;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface ProfileMapper {
    ProfileCreationRequest toProfileCreationRequest(UserCreationRequest request);
}
