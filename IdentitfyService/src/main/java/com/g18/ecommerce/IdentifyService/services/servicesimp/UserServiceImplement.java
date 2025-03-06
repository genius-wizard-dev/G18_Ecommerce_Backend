package com.g18.ecommerce.IdentifyService.services.servicesimp;

import com.g18.ecommerce.IdentifyService.dto.request.ProfileCreationRequest;
import com.g18.ecommerce.IdentifyService.dto.request.UserCreationRequest;
import com.g18.ecommerce.IdentifyService.dto.response.ProfileResponse;
import com.g18.ecommerce.IdentifyService.dto.response.UserResponse;
import com.g18.ecommerce.IdentifyService.entity.Role;
import com.g18.ecommerce.IdentifyService.entity.Status;
import com.g18.ecommerce.IdentifyService.entity.User;
import com.g18.ecommerce.IdentifyService.exception.AppException;
import com.g18.ecommerce.IdentifyService.exception.ErrorCode;
import com.g18.ecommerce.IdentifyService.mapper.ProfileMapper;
import com.g18.ecommerce.IdentifyService.mapper.UserMapper;
import com.g18.ecommerce.IdentifyService.repositories.RoleRepository;
import com.g18.ecommerce.IdentifyService.repositories.UserRepository;
import com.g18.ecommerce.IdentifyService.services.UserService;
import com.g18.ecommerce.IdentifyService.utils.Constant;
import com.google.gson.Gson;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.header.internals.RecordHeader;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.requestreply.ReplyingKafkaTemplate;
import org.springframework.kafka.requestreply.RequestReplyFuture;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Date;
import java.util.HashSet;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class UserServiceImplement implements UserService {

    UserRepository userRepository;
    RoleRepository roleRepository;
    UserMapper userMapper;
    PasswordEncoder passwordEncoder;
    ProfileMapper profileMapper;
    Gson gson;
    KafkaTemplate<String, String> kafkaTemplate;
    ReplyingKafkaTemplate<String, String, String> replyingKafkaTemplate;

    @Override
    public UserResponse createUser(UserCreationRequest request) {
        User user = userMapper.toUser(request);
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setStatus(Status.valueOf(Constant.ACTIVE));
        user.setCreatedAt(new Date(Instant.now().toEpochMilli()));
        user.setUpdatedAt(new Date(Instant.now().toEpochMilli()));
        HashSet<Role> roles = new HashSet<>();
        roleRepository.findById(Constant.ROLE_USER).ifPresent(roles::add);
        user.setRoles(roles);
        try {
            user = userRepository.save(user);
        } catch (Exception e) {
            throw new AppException(ErrorCode.CREATE_USER_ERROR);
        }
        ProfileCreationRequest profileRequest = profileMapper.toProfileCreationRequest(request);
        profileRequest.setUserId(user.getId());
        ProducerRecord<String, String> record = new ProducerRecord<>(Constant.PROFILE_ONBOARDING, gson.toJson(profileRequest));

        record.headers().add(new RecordHeader(KafkaHeaders.REPLY_TOPIC, Constant.PROFILE_ONBOARDED.getBytes(StandardCharsets.UTF_8)));
        RequestReplyFuture<String, String, String> replyFuture =
                replyingKafkaTemplate.sendAndReceive(record);
        try {
            ConsumerRecord<String, String> consumerRecord = replyFuture.get(10, TimeUnit.SECONDS);
            String profileResponseJson = consumerRecord.value();
            ProfileResponse profileResponse = gson.fromJson(profileResponseJson, ProfileResponse.class);
            log.info("Received profileResponse: {}", profileResponse);
            var userResponse = userMapper.toUserResponse(user);
            userResponse.setProfileId(profileResponse.getId());
            return  userResponse;
        } catch (Exception e) {
            throw new AppException(ErrorCode.CREATE_PROFILE_ERROR);
        }


    }

    @Override
    public UserResponse getMyInfo() {
        var context = SecurityContextHolder.getContext();
        String name = context.getAuthentication().getName();
        User user = userRepository.findByUsername(name).orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));
        return userMapper.toUserResponse(user);
    }

//    @Override
//    public UserResponse updateUser(String userId, UserUpdateRequest request) {
//        var foundUser = userRepository.findById(userId).orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));
//        userMapper.updateUser(foundUser, request);
//        foundUser.setPassword(passwordEncoder.encode(request.getPassword()));
//        foundUser.setUpdatedAt(new Date(Instant.now().toEpochMilli()));
//        var roles = roleRepository.findAllById(request.getRoles());
//        foundUser.setRoles(new HashSet<>(roles));
//
//        return userMapper.toUserResponse(userRepository.save(foundUser));
//    }

    @PreAuthorize("hasRole('ADMIN')")
    @Override
    public UserResponse deleteUser(String userId) {
        log.info("Delete user with id: {}", userId);
        var foundUser = userRepository.findById(userId).orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));
        foundUser.setStatus(Status.valueOf(Constant.DELETED));
        kafkaTemplate.send(Constant.SUSPEND_PROFILE, userId);
        return userMapper.toUserResponse(userRepository.save(foundUser));
    }

    @Override
    @PreAuthorize("hasRole('ADMIN')")
    public UserResponse getUser(String id) {
        return userMapper.toUserResponse(userRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED)));
    }
}
