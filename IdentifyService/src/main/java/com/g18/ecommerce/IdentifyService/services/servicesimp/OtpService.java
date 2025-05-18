package com.g18.ecommerce.IdentifyService.services.servicesimp;

import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;

@Service
@FieldDefaults(level = lombok.AccessLevel.PRIVATE, makeFinal = true)
@RequiredArgsConstructor
public class OtpService {
    StringRedisTemplate stringRedisTemplate;
    private static final Duration OTP_EXPIRATION = Duration.ofMinutes(5);

    public void saveOtp(String userId, String otp) {
        stringRedisTemplate.opsForValue().set("otp:" + userId, otp, OTP_EXPIRATION);
    }
    public boolean verifyOtp(String userId, String otp) {
        String cachedOtp = stringRedisTemplate.opsForValue().get("otp:" + userId);
        return otp != null && otp.equals(cachedOtp);
    }
    public void deleteOtp(String userId) {
        stringRedisTemplate.delete("otp:" + userId);
    }
}
