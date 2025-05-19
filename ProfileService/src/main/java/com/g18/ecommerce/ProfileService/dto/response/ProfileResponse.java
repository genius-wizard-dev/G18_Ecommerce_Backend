package com.g18.ecommerce.ProfileService.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;

import java.util.Date;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = lombok.AccessLevel.PRIVATE)
public class ProfileResponse {
    String id;
    String userId;
    String email;
    String phoneNumber;
    String fullName;
    String shopId;
    Date birthDay;

}
