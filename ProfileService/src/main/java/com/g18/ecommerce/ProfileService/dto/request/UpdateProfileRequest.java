package com.g18.ecommerce.ProfileService.dto.request;


import com.g18.ecommerce.ProfileService.validator.DobConstraint;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.format.annotation.DateTimeFormat;

import java.util.Date;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = lombok.AccessLevel.PRIVATE)
public class UpdateProfileRequest {

    @Pattern(
            regexp = "^(https?|ftp)://[^\\s/$.?#].[^\\s]*$",
            message = "INVALID_AVATAR_URL"
    )
    String avatar;

    @Size(max = 10, message = "INVALID_DISPLAY_NAME")
    String displayName;

    @DobConstraint(min = 16, message = "INVALID_DOB")
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    Date birthDay;
}
