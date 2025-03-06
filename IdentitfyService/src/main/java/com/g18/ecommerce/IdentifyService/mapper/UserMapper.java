package com.g18.ecommerce.IdentifyService.mapper;

import com.g18.ecommerce.IdentifyService.dto.request.UserCreationRequest;
import com.g18.ecommerce.IdentifyService.dto.request.UserUpdateRequest;
import com.g18.ecommerce.IdentifyService.dto.response.UserResponse;
import com.g18.ecommerce.IdentifyService.entity.User;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface UserMapper {
    User toUser(UserCreationRequest request);

    UserResponse toUserResponse(User user);

//    @Mapping(target = "roles", ignore = true)
//    void updateUser(User user, UserUpdateRequest request);
}
