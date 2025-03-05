package com.g18.ecommerce.ProfileService.services.servicesimp;

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
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Date;
import java.util.Objects;
import java.util.UUID;

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
        ProfileCreationRequest profileCreationRequest = gson.fromJson(request, ProfileCreationRequest.class);
        Profile profile = profileMapper.toProfile(profileCreationRequest);
        profile.setCreatedAt(new Date(Instant.now().toEpochMilli()));
        profile.setUpdatedAt(new Date(Instant.now().toEpochMilli()));
        profile.setActivated(true);
        profile = profileRepository.save(profile);
        return gson.toJson(profileMapper.toProfileResponse(profile));
    }

    @Override
    public ProfileResponse updateProfile(String id, UpdateProfileRequest request) {
        var foundProfile = profileRepository.findByUserId(id).
                orElseThrow(() -> new AppException(ErrorCode.PROFILE_NOT_FOUND));
        foundProfile.setAvatar(request.getAvatar());
        foundProfile.setDisplayName(request.getDisplayName());
        foundProfile.setBirthDay(request.getBirthDay());
        foundProfile.setUpdatedAt(new Date(Instant.now().toEpochMilli()));
        return profileMapper.toProfileResponse(profileRepository.save(foundProfile));
    }

    @Override
    @KafkaListener(topics = Constant.SUSPEND_PROFILE, groupId = "profile-group")
    public void inactiveProfile(String userId) {
        var foundProfile = profileRepository.findByUserId(userId).
                orElseThrow(() -> new AppException(ErrorCode.PROFILE_NOT_FOUND));
        foundProfile.setActivated(false);
        profileRepository.save(foundProfile);
    }

    @Override
    public ProfileResponse registerShop(String profileId) {
        var foundProfile = profileRepository.findById(profileId).
                orElseThrow(() -> new AppException(ErrorCode.PROFILE_NOT_FOUND));
        if(!Objects.isNull(foundProfile.getShopId()))
            foundProfile.setShopId(UUID.randomUUID().toString());
        var response = profileRepository.save(foundProfile);
        return profileMapper.toProfileResponse(response);
    }
}
