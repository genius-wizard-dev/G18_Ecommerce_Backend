package com.g18.ecommerce.ProfileService.services.servicesimp;

import java.time.Instant;
import java.util.Date;
import java.util.Objects;
import java.util.UUID;

import com.g18.ecommerce.ProfileService.dto.request.RegisterShopRequest;
import io.github.resilience4j.retry.annotation.Retry;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.messaging.handler.annotation.SendTo;
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

    // @Override
    // @KafkaListener(topics = Constant.PROFILE_ONBOARDING, groupId =
    // "profile-group")
    // @SendTo(Constant.PROFILE_ONBOARDED)
    // public String createProfile(String request) {
    // log.info("Received message: {}", request);
    // ProfileCreationRequest profileCreationRequest = gson.fromJson(request,
    // ProfileCreationRequest.class);
    // Profile profile = profileMapper.toProfile(profileCreationRequest);
    // profile.setCreatedAt(new Date(Instant.now().toEpochMilli()));
    // profile.setUpdatedAt(new Date(Instant.now().toEpochMilli()));
    // profile.setActivated(true);
    // profile = profileRepository.save(profile);
    // return gson.toJson(profileMapper.toProfileResponse(profile));
    // }

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
                jsonString = jsonString.substring(1, jsonString.length() - 1);
                jsonString = jsonString.replace("\\\"", "\"")
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
    @Retry(name = "default", fallbackMethod = "fallbackMethod")
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

    @Override
    @KafkaListener(topics = Constant.SUSPEND_PROFILE, groupId = "profile-group")
    public void inactiveProfile(String userId) {
        var foundProfile = profileRepository.findByUserId(userId)
                .orElseThrow(() -> new AppException(ErrorCode.PROFILE_NOT_FOUND));
        foundProfile.setActivated(false);
        profileRepository.save(foundProfile);
    }

    @Override
    @Retry(name = "default", fallbackMethod = "fallbackMethod")
    public ProfileResponse registerShop(String profileId, RegisterShopRequest req) {
        var foundProfile = profileRepository.findById(profileId)
                .orElseThrow(() -> new AppException(ErrorCode.PROFILE_NOT_FOUND));
        if (Objects.isNull(foundProfile.getShopId()))
            foundProfile.setShopId(UUID.randomUUID().toString());
        foundProfile.setShopName(req.getShopName());
        var response = profileRepository.save(foundProfile);
        return profileMapper.toProfileResponse(response);
    }

    @Override
    @Retry(name = "default", fallbackMethod = "fallbackMethod")
    public ProfileResponse getProfileByUserId(String userId) {
        var foundProfile = profileRepository.findByUserId(userId)
                .orElseThrow(() -> new AppException(ErrorCode.PROFILE_NOT_FOUND));
        return profileMapper.toProfileResponse(foundProfile);
    }

    @Override
    @Retry(name = "default", fallbackMethod = "fallbackMethod")
    public boolean checkIsShop(String userId) {
        var foundProfile = profileRepository.findByUserId(userId)
                .orElseThrow(() -> new AppException(ErrorCode.PROFILE_NOT_FOUND));
        return foundProfile.getShopId() != null;
    }
    public String fallbackMethod(Exception ex) {
        return "Hệ thống hiện đang bận, vui lòng thử lại sau.";
    }
}
