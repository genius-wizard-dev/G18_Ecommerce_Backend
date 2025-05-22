package com.g18.ecommerce.IdentifyService.services.servicesimp;

import com.g18.ecommerce.IdentifyService.dto.request.*;
import com.g18.ecommerce.IdentifyService.dto.response.AuthenticationResponse;
import com.g18.ecommerce.IdentifyService.dto.response.IntrospectResponse;
import com.g18.ecommerce.IdentifyService.entity.InvalidatedToken;
import com.g18.ecommerce.IdentifyService.entity.Status;
import com.g18.ecommerce.IdentifyService.entity.User;
import com.g18.ecommerce.IdentifyService.exception.AppException;
import com.g18.ecommerce.IdentifyService.exception.ErrorCode;
import com.g18.ecommerce.IdentifyService.repositories.InvalidatedRepository;
import com.g18.ecommerce.IdentifyService.repositories.UserRepository;
import com.g18.ecommerce.IdentifyService.services.AuthenticationService;
import com.nimbusds.jose.*;
import com.nimbusds.jose.crypto.MACSigner;
import com.nimbusds.jose.crypto.MACVerifier;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import io.github.resilience4j.retry.annotation.Retry;
import jakarta.mail.MessagingException;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.experimental.NonFinal;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.text.ParseException;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.Random;
import java.util.StringJoiner;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = lombok.AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class AuthenticationServiceImplement implements AuthenticationService {
    UserRepository userRepository;
    InvalidatedRepository invalidatedRepository;
    PasswordEncoder passwordEncoder;
    @Value("${jwt.signerKey}")
    @NonFinal
    protected String SIGNER_KEY;


    @NonFinal
    @Value("${jwt.valid-duration}")
    protected long DURATION_TIME;


    @NonFinal
    @Value("${jwt.refreshable-duration}")
    protected long REFRESHABLE_DURATION;

    OtpService otpService;

    EmailService emailService;

    @Override
    @Retry(name = "default", fallbackMethod = "fallbackMethod")
    public AuthenticationResponse authenticate(AuthenticationRequest req) {
        var user = userRepository.findByUsername(req.getUsername())
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));
        boolean isAuthenticated = passwordEncoder.matches(req.getPassword(), user.getPassword());
        if (!isAuthenticated) throw new AppException(ErrorCode.UNAUTHENTICATED);
        if (user.getStatus().equals(Status.DELETED)) throw new AppException(ErrorCode.ACCOUNT_DELETED);
        if (user.getStatus().equals(Status.SUSPENDED)) throw new AppException(ErrorCode.ACCOUNT_LOCKED);
        var token = generateToken(user);
        return AuthenticationResponse.builder()
                .token(token)
                .expiryTime(new Date(Instant.now().plus(DURATION_TIME, ChronoUnit.SECONDS).toEpochMilli()))
                .build();
    }

    @Override
    @Retry(name = "default", fallbackMethod = "fallbackMethod")
    public IntrospectResponse introspect(IntrospectRequest req) {
        var token = req.getToken();
        boolean isValid = true;
        try {
            verifyToken(token, false);
        } catch (AppException | JOSEException | ParseException e) {
            throw new AppException(ErrorCode.INVALID_TOKEN);
        }
        return IntrospectResponse.builder().valid(isValid).build();
    }

    @Override
    @Retry(name = "default", fallbackMethod = "fallbackMethod")
    public void logout(LogoutRequest req) {
        try {
            var signToken = verifyToken(req.getToken(), true);
            String jwtID = signToken.getJWTClaimsSet().getJWTID();
            Date expiryTime = signToken.getJWTClaimsSet().getExpirationTime();
            InvalidatedToken invalidatedToken = InvalidatedToken.builder()
                    .id(jwtID)
                    .expiryTime(expiryTime)
                    .build();
            invalidatedRepository.save(invalidatedToken);
        } catch (AppException | JOSEException | ParseException e) {
            throw new AppException(ErrorCode.UNCATEGORIZED_EXCEPTION);
        }
    }

    @Override
    @Retry(name = "default", fallbackMethod = "fallbackMethod")
    public AuthenticationResponse refreshToken(RefreshRequest req) {
        try {
            var signedJWT = verifyToken(req.getToken(), true);
            String jwtID = signedJWT.getJWTClaimsSet().getJWTID();
            Date expiryTime = signedJWT.getJWTClaimsSet().getExpirationTime();
            InvalidatedToken invalidatedToken = InvalidatedToken.builder()
                    .id(jwtID)
                    .expiryTime(expiryTime)
                    .build();
            invalidatedRepository.save(invalidatedToken);
            var username = signedJWT.getJWTClaimsSet().getSubject();
            var user = userRepository.findByUsername(username)
                    .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));
            var token = generateToken(user);
            return AuthenticationResponse.builder()
                    .token(token)
                    .expiryTime(new Date(Instant.now().plus(DURATION_TIME, ChronoUnit.SECONDS).toEpochMilli()))
                    .build();
        } catch (JOSEException | ParseException e) {
            throw new AppException(ErrorCode.REFRESH_TOKEN_ERROR);
        }
    }

    @Override
    @Retry(name = "default", fallbackMethod = "fallbackMethod")
    public boolean changePassword(String userId, ChangePasswordRequest req) {
        var user = userRepository.findById(userId)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));

        if (!otpService.verifyOtp(userId, req.getOtp())) {
            throw new AppException(ErrorCode.INVALID_OTP);
        }

        if (!passwordEncoder.matches(req.getOldPassword(), user.getPassword())) {
            throw new AppException(ErrorCode.PASSWORD_NOT_MATCH);
        }

        user.setPassword(passwordEncoder.encode(req.getNewPassword()));
        userRepository.save(user);
        otpService.deleteOtp(userId);

        return true;
    }

    @Override
    @Retry(name = "default", fallbackMethod = "fallbackMethod")
    public String sendOtp(String userId, OtpRequest req) throws MessagingException {
        var user = userRepository.findById(userId)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));

        String otp = String.valueOf(new Random().nextInt(900000) + 100000);
        otpService.saveOtp(userId, otp);
        emailService.send(req.getEmail(), buildOtpHtmlContent(otp));
        return "Mã OTP đã được gửi đến email của bạn";
    }

    private String buildOtpHtmlContent(String otp) {
        return "<div style='font-family: Arial, sans-serif; line-height: 1.6;'>"
                + "<h2 style='color: #2E86C1;'>G18 Ecommerce - Xác nhận đổi mật khẩu</h2>"
                + "<p>Xin chào,</p>"
                + "<p>Chúng tôi đã nhận được yêu cầu đổi mật khẩu từ tài khoản của bạn.</p>"
                + "<p>Vui lòng sử dụng mã OTP bên dưới để xác nhận:</p>"
                + "<h1 style='color: #E74C3C;'>" + otp + "</h1>"
                + "<p>Mã OTP này có hiệu lực trong 5 phút.</p>"
                + "<p>Nếu bạn không thực hiện yêu cầu này, vui lòng bỏ qua email này.</p>"
                + "<br/>"
                + "<p>Trân trọng,</p>"
                + "<p><strong>G18 Ecommerce</strong></p>"
                + "</div>";
    }

    private String generateToken(User user) {
        JWSHeader header = new JWSHeader(JWSAlgorithm.HS512);

        JWTClaimsSet jwtClaimsSet = new JWTClaimsSet.Builder()
                .subject(user.getUsername())
                .issuer("G18 Ecommerce")
                .issueTime(new Date())
                .expirationTime(
                        new Date(Instant.now().plus(DURATION_TIME, ChronoUnit.SECONDS).toEpochMilli())
                )
                .jwtID(UUID.randomUUID().toString())
                .claim("scope", buildScope(user))
                .build();
        Payload payload = new Payload(jwtClaimsSet.toJSONObject());
        JWSObject jwsObject = new JWSObject(header, payload);
        try {
            jwsObject.sign(new MACSigner(SIGNER_KEY));
            return jwsObject.serialize();
        } catch (Exception e) {
            throw new AppException(ErrorCode.GENERATE_TOKEN_ERROR);
        }
    }

    private SignedJWT verifyToken(String token, boolean isRefresh) throws JOSEException, ParseException {
        log.info("Verifying token: {}", token);
        JWSVerifier verifier = new MACVerifier(SIGNER_KEY.getBytes());
        SignedJWT signedJWT = SignedJWT.parse(token);
        Date expiryTime = (isRefresh)
                ? new Date(signedJWT.getJWTClaimsSet().getIssueTime().toInstant()
                .plus(REFRESHABLE_DURATION, ChronoUnit.SECONDS).toEpochMilli())
                : signedJWT.getJWTClaimsSet().getExpirationTime();
        var verified = signedJWT.verify(verifier);
        if (!(verified && expiryTime.after(new Date()))) throw new AppException(ErrorCode.UNAUTHENTICATED);
        if (invalidatedRepository.existsById(signedJWT.getJWTClaimsSet().getJWTID()))
            throw new AppException(ErrorCode.UNAUTHENTICATED);
        return signedJWT;
    }

    private String buildScope(User user) {
        StringJoiner stringJoiner = new StringJoiner(" ");
        if (!CollectionUtils.isEmpty(user.getRoles())) {
            user.getRoles().forEach(role -> {
                stringJoiner.add("ROLE_" + role.getName());
                if (!CollectionUtils.isEmpty(role.getPermissions())) {
                    role.getPermissions().forEach(permission -> {
                        stringJoiner.add(permission.getName());
                    });
                }
            });
        }
        return stringJoiner.toString();
    }

    // Fallback cho authenticate
    public AuthenticationResponse fallbackMethod(AuthenticationRequest req, Throwable ex) {
        return AuthenticationResponse.builder()
                .token("fallback-token")
                .expiryTime(new Date(System.currentTimeMillis() + 60000)) // Token tạm
                .build();
    }

    // Fallback cho introspect
    public IntrospectResponse fallbackMethod(IntrospectRequest req, Throwable ex) {
        return IntrospectResponse.builder()
                .valid(false)
                .build();
    }

    // Fallback cho logout (void)
    public void fallbackMethod(LogoutRequest req, Throwable ex) {
        log.error("Fallback logout: {}", ex.getMessage());
    }

    // Fallback cho refreshToken
    public AuthenticationResponse fallbackMethod(RefreshRequest req, Throwable ex) {
        return AuthenticationResponse.builder()
                .token("fallback-refresh-token")
                .expiryTime(new Date(System.currentTimeMillis() + 60000))
                .build();
    }

    // Fallback cho changePassword
    public boolean fallbackMethod(String userId, ChangePasswordRequest req, Throwable ex) {
        log.error("Fallback changePassword: {}", ex.getMessage());
        return false;
    }

    // Fallback cho sendOtp
    public String fallbackMethod(String userId, OtpRequest req, Throwable ex) {
        log.error("Fallback sendOtp: {}", ex.getMessage());
        return "Không thể gửi mã OTP lúc này, vui lòng thử lại sau.";
    }

}
