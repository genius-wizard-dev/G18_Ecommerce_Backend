package com.g18.ecommerce.IdentifyService.services;


import com.g18.ecommerce.IdentifyService.dto.request.AuthenticationRequest;
import com.g18.ecommerce.IdentifyService.dto.request.IntrospectRequest;
import com.g18.ecommerce.IdentifyService.dto.request.LogoutRequest;
import com.g18.ecommerce.IdentifyService.dto.request.RefreshRequest;
import com.g18.ecommerce.IdentifyService.dto.response.AuthenticationResponse;
import com.g18.ecommerce.IdentifyService.dto.response.IntrospectResponse;
import com.g18.ecommerce.IdentifyService.repositories.UserRepository;
import com.nimbusds.jose.JOSEException;
import org.springframework.stereotype.Service;

import java.text.ParseException;


public interface AuthenticationService {
   public AuthenticationResponse authenticate(AuthenticationRequest req);
   public IntrospectResponse introspect(IntrospectRequest req);
   public void logout(LogoutRequest req);
   public AuthenticationResponse refreshToken(RefreshRequest req);
}
