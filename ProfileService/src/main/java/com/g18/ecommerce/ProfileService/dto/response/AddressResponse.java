package com.g18.ecommerce.ProfileService.dto.response;

import com.g18.ecommerce.ProfileService.entity.Profile;
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
public class AddressResponse {
    String id;
    String profileId;
    String street;
    String city;
    String detail;
    String type;
    String phoneShip;
    boolean isDefault;
    Date createdAt;
    Date updatedAt;
}
