package com.g18.ecommerce.IdentifyService.controllers;

import com.g18.ecommerce.IdentifyService.dto.request.ApiResponse;
import com.g18.ecommerce.IdentifyService.dto.request.UserCreationRequest;
import com.g18.ecommerce.IdentifyService.dto.response.UserResponse;
import com.g18.ecommerce.IdentifyService.services.UserService;
import com.g18.ecommerce.IdentifyService.utils.CommonFunction;
import com.g18.ecommerce.IdentifyService.utils.Constant;
import com.google.gson.Gson;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.io.InputStream;

@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
@FieldDefaults(level = lombok.AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class UserController {
    UserService userService;
    Gson gson;
    @PostMapping("/sign-up")
    ApiResponse<UserResponse> createUser(@RequestBody UserCreationRequest req) {
        InputStream inputStream = UserController.class.getClassLoader().getResourceAsStream(Constant.JSON_REQ_CREATE_USER);
        CommonFunction.jsonValidate(inputStream, gson.toJson(req));
        return ApiResponse.<UserResponse>builder()
                .result(userService.createUser(req))
                .build();
    }
    @GetMapping("/my-info")
    ApiResponse<UserResponse> getMyInfo() {
        return ApiResponse.<UserResponse>builder()
                .result(userService.getMyInfo())
                .build();
    }
    @PutMapping("/{id}")
    ApiResponse<UserResponse> inactiveUser(@PathVariable String id){
        return ApiResponse.<UserResponse>builder()
                .result(userService.deleteUser(id))
                .build();
    }
}
