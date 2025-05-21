package com.g18.ecommerce.ProfileService.services.servicesimp;

import java.time.Instant;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

import com.g18.ecommerce.ProfileService.dto.request.RegisterShopRequest;
import io.github.resilience4j.retry.annotation.Retry;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;

import com.g18.ecommerce.ProfileService.dto.request.ProfileCreationRequest;
import com.g18.ecommerce.ProfileService.dto.request.UpdateProfileRequest;
import com.g18.ecommerce.ProfileService.dto.response.ProfileResponse;
import com.g18.ecommerce.ProfileService.entity.Profile;
import com.g18.ecommerce.ProfileService.exception.AppException;
import com.g18.ecommerce.ProfileService.exception.ErrorCode;
import com.g18.ecommerce.ProfileService.mapper.ProfileMapper;
import com.g18.ecommerce.ProfileService.repositories.ProfileRepository;
import com.g18.ecommerce.ProfileService.services.ProfileService;
import com.g18.ecommerce.ProfileService.utils.Constant;
import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;

import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = lombok.AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class ProfilesServiceImpl implements ProfileService {

    ProfileRepository profileRepository;
    ProfileMapper profileMapper;
    Gson gson;

    @Override
    @KafkaListener(topics = Constant.PROFILE_ONBOARDING, groupId = "profile-group")
    @SendTo(Constant.PROFILE_ONBOARDED)
    public String createProfile(String request) {
        log.info("Received message: {}", request);
        if (request == null || request.trim().isEmpty()) {
            log.error("Invalid message: null or empty");
            throw new IllegalArgumentException("Message is null or empty");
        }
        try {
            String jsonString = request;
            if (jsonString.startsWith("\"") && jsonString.endsWith("\"")) {
                jsonString = jsonString.substring(1, jsonString.length() - 1)
                        .replace("\\\"", "\"")
                        .replace("\\\\", "\\")
                        .replace("\\/", "/");
                log.info("Processed JSON string: {}", jsonString);
            }

            ProfileCreationRequest profileCreationRequest = gson.fromJson(jsonString, ProfileCreationRequest.class);
            if (profileCreationRequest == null) {
                log.error("Parsed request is null");
                throw new IllegalArgumentException("Invalid JSON format");
            }
            Profile profile = profileMapper.toProfile(profileCreationRequest);
            profile.setCreatedAt(new Date(Instant.now().toEpochMilli()));
            profile.setUpdatedAt(new Date(Instant.now().toEpochMilli()));
            profile.setActivated(true);
            profile = profileRepository.save(profile);
            String response = gson.toJson(profileMapper.toProfileResponse(profile));
            log.info("Sending response: {}", response);
            return response;
        } catch (JsonSyntaxException e) {
            log.error("Failed to parse JSON: {}", request, e);
            throw new IllegalArgumentException("Invalid JSON format", e);
        } catch (Exception e) {
            log.error("Processing error for message: {}", request, e);
            throw new RuntimeException("Processing error", e);
        }
    }

    @Override
    @KafkaListener(topics = Constant.SUSPEND_PROFILE, groupId = "profile-group")
    public void inactiveProfile(String userId) {
        var foundProfile = profileRepository.findByUserId(userId)
                .orElseThrow(() -> new AppException(ErrorCode.PROFILE_NOT_FOUND));
        foundProfile.setActivated(false);
        profileRepository.save(foundProfile);
    }

    @Override
    @Retry(name = "default", fallbackMethod = "fallbackUpdateProfile")
    public ProfileResponse updateProfile(String id, UpdateProfileRequest request) {
        var foundProfile = profileRepository.findByUserId(id)
                .orElseThrow(() -> new AppException(ErrorCode.PROFILE_NOT_FOUND));
        foundProfile.setAvatar(request.getAvatar());
        foundProfile.setEmail(request.getEmail());
        foundProfile.setPhoneNumber(request.getPhoneNumber());
        foundProfile.setFullName(request.getFullName());
        foundProfile.setBirthDay(request.getBirthDay());
        foundProfile.setUpdatedAt(new Date(Instant.now().toEpochMilli()));
        return profileMapper.toProfileResponse(profileRepository.save(foundProfile));
    }

    public ProfileResponse fallbackUpdateProfile(String id, UpdateProfileRequest request, Throwable ex) {
        log.error("Fallback: updateProfile", ex);
        throw new RuntimeException("Hệ thống hiện đang bận, vui lòng thử lại sau.");
    }

    @Override
    @Retry(name = "default", fallbackMethod = "fallbackRegisterShop")
    public ProfileResponse registerShop(String profileId, RegisterShopRequest req) {
        var foundProfile = profileRepository.findById(profileId)
                .orElseThrow(() -> new AppException(ErrorCode.PROFILE_NOT_FOUND));
        if (Objects.isNull(foundProfile.getShopId()))
            foundProfile.setShopId(UUID.randomUUID().toString());
        foundProfile.setShopName(req.getShopName());
        var response = profileRepository.save(foundProfile);
        return profileMapper.toProfileResponse(response);
    }

    public ProfileResponse fallbackRegisterShop(String profileId, RegisterShopRequest req, Throwable ex) {
        log.error("Fallback: registerShop", ex);
        throw new RuntimeException("Không thể đăng ký shop lúc này.");
    }

    @Override
    @Retry(name = "default", fallbackMethod = "fallbackGetProfileByUserId")
    public ProfileResponse getProfileByUserId(String userId) {
        var foundProfile = profileRepository.findByUserId(userId)
                .orElseThrow(() -> new AppException(ErrorCode.PROFILE_NOT_FOUND));
        return profileMapper.toProfileResponse(foundProfile);
    }

    public ProfileResponse fallbackGetProfileByUserId(String userId, Throwable ex) {
        log.error("Fallback: getProfileByUserId", ex);
        throw new RuntimeException("Không thể lấy thông tin người dùng lúc này.");
    }

    @Override
    @Retry(name = "default", fallbackMethod = "fallbackCheckIsShop")
    public boolean checkIsShop(String userId) {
        var foundProfile = profileRepository.findByUserId(userId)
                .orElseThrow(() -> new AppException(ErrorCode.PROFILE_NOT_FOUND));
        return foundProfile.getShopId() != null;
    }

    public boolean fallbackCheckIsShop(String userId, Throwable ex) {
        log.error("Fallback: checkIsShop", ex);
        return false;
    }

    @Override
    @Retry(name = "default", fallbackMethod = "fallbackGetUserByShopId")
    public ProfileResponse getUserByShopId(String shopId) {
        var foundProfile = profileRepository.findByShopId(shopId)
                .orElseThrow(() -> new AppException(ErrorCode.PROFILE_NOT_FOUND));
        return profileMapper.toProfileResponse(foundProfile);
    }

    public ProfileResponse fallbackGetUserByShopId(String shopId, Throwable ex) {
        log.error("Fallback: getUserByShopId", ex);
        throw new RuntimeException("Không thể tìm người dùng theo shop lúc này.");
    }

    @Override
    @Retry(name = "default", fallbackMethod = "fallbackFindShopsByName")
    public List<ProfileResponse> findShopsByName(String name) {
        return profileRepository.findByShopNameContaining(name).stream()
                .map(profileMapper::toProfileResponse)
                .toList();
    }

    public List<ProfileResponse> fallbackFindShopsByName(String name, Throwable ex) {
        log.error("Fallback: findShopsByName", ex);
        return Collections.emptyList();
    }

    @Override
    @PreAuthorize("hasRole('ADMIN')")
    @Retry(name = "default", fallbackMethod = "fallbackGetAllShop")
    public List<ProfileResponse> getAllShop() {
        return profileRepository.findByShopIdIsNotNull().stream()
                .map(profileMapper::toProfileResponse)
                .toList();
    }

    public List<ProfileResponse> fallbackGetAllShop(Throwable ex) {
        log.error("Fallback: getAllShop", ex);
        return Collections.emptyList();
    }
}
