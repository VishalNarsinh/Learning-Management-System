package com.lms.controller;

import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.googleapis.util.Utils;
import com.google.api.client.json.JsonFactory;
import com.lms.dto.*;
import com.lms.exception.ApiException;
import com.lms.mapper.UserMapper;
import com.lms.model.Image;
import com.lms.model.User;
import com.lms.repository.ImageRepository;
import com.lms.repository.UserRepository;
import com.lms.security.jwt.JwtUtil;
import com.lms.service.MyUserDetailsService;
import com.lms.utils.AppConstants;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Collections;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {

    private final Logger logger = LoggerFactory.getLogger(AuthController.class);
    private final MyUserDetailsService userDetailsService;
    private final AuthenticationManager authenticationManager;
    private final JwtUtil jwtUtil;
    private final UserRepository userRepository;
    private final ImageRepository imageRepository;
    private final PasswordEncoder passwordEncoder;

    @Value("${google.client.id}")
    private String CLIENT_ID;

    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody RegisterRequest registerRequest) {
        UserDto userDto = userDetailsService.saveUser(registerRequest);
        return ResponseEntity.ok(userDto);
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest loginRequest) {
        logger.info("{}", loginRequest);
        doAuthenticate(loginRequest.getEmail(), loginRequest.getPassword());
        UserDetails userDetails = userDetailsService.loadUserByUsername(loginRequest.getEmail());
        String token = jwtUtil.generateToken(userDetails);
        String refreshToken = jwtUtil.generateRefreshToken(userDetails);

        LoginResponse response = LoginResponse.builder()
                .user(UserMapper.toDto((User) userDetails))
                .token(token)
                .refreshToken(refreshToken)
                .build();
        return ResponseEntity.ok(response);
    }

    private void doAuthenticate(String email, String password) {
//        jacksonObjectMapper
        UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(email, password);
        try {
            authenticationManager.authenticate(authentication);
        } catch (BadCredentialsException e) {
            logger.info("{}",e.getLocalizedMessage());
            throw new ApiException(" Invalid Username or Password !!");
        }

    }


    @PostMapping("/google-login")
    public ResponseEntity<?> googleLogin(@RequestBody GoogleLoginRequest request) {
        try {
            JsonFactory jsonFactory = Utils.getDefaultJsonFactory();
            GoogleIdTokenVerifier verifier = new GoogleIdTokenVerifier.Builder(
                    GoogleNetHttpTransport.newTrustedTransport(),
                    jsonFactory
            )
                    .setAudience(Collections.singletonList(CLIENT_ID))
                    .build();

            logger.info("Client ID: {}", CLIENT_ID);
            logger.info("Incoming ID Token: {}", request.getIdToken());

            GoogleIdToken idToken = verifier.verify(request.getIdToken());

            if (idToken == null) {
                logger.error("Invalid Google token received");
                return ResponseEntity.badRequest().body("Invalid Google token");
            }

            GoogleIdToken.Payload payload = idToken.getPayload();
            String email = payload.getEmail();
            String name = (String) payload.get("name");

            logger.info("Verified Email: {}", email);
            logger.info("User Name: {}", name);

            // Find an existing user or create a new one
            User user = userRepository.findByEmail(email).orElse(null);
            if (user == null) {
                user = new User();
                user.setEmail(email);
                user.setFirstName(name);
                user.setEnabled(true);
                user.setRole(request.getRole());
                user.setPassword(passwordEncoder.encode(UUID.randomUUID().toString()));
                String googlePicture = payload.get("picture").toString();
                Image image = Image
                        .builder()
                        .imageUrl(googlePicture != null ? googlePicture : AppConstants.DEFAULT_USER_IMAGE)
                        .build();
                Image saved = imageRepository.save(image);
                user.setImage(saved);


                user = userRepository.save(user);
                logger.info("New user created: {}", user.getEmail());
            }else{
                logger.info("User already exists: {}", user.getEmail());
            }

            // Generate JWT token
            String jwtToken = jwtUtil.generateToken(userDetailsService.loadUserByUsername(email));
            String refreshToken = jwtUtil.generateRefreshToken(userDetailsService.loadUserByUsername(email));
            return ResponseEntity.ok(LoginResponse.builder()
                    .user(UserMapper.toDto(user))
                    .token(jwtToken)
                    .refreshToken(refreshToken)
                    .build()
            );
        } catch (Exception e) {
            logger.error("Google authentication failed", e);
            return ResponseEntity.badRequest().body("Google authentication failed: " + e.getMessage());
        }
    }


}
