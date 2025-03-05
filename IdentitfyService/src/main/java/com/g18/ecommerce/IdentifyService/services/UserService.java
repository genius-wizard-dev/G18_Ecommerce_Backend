package com.g18.ecommerce.IdentifyService.services;

import com.g18.ecommerce.IdentifyService.dto.request.UserCreationRequest;
import com.g18.ecommerce.IdentifyService.dto.request.UserUpdateRequest;
import com.g18.ecommerce.IdentifyService.dto.response.UserResponse;
import org.springframework.stereotype.Service;


public interface UserService {
    public UserResponse createUser(UserCreationRequest request);
    public UserResponse getMyInfo();
//    public UserResponse updateUser(String userId, UserUpdateRequest request);
    public UserResponse deleteUser(String userId);
    public UserResponse getUser(String id);
}
