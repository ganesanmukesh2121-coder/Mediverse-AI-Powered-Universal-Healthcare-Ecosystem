package com.mediverse.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.mediverse.dto.LoginRequestDto;
import com.mediverse.dto.LoginResponseDto;
import com.mediverse.dto.MessageResponseDto;
import com.mediverse.dto.RefreshTokenRequestDto;
import com.mediverse.dto.RefreshTokenResponseDto;
import com.mediverse.dto.RegisterRequestDto;
import com.mediverse.dto.RegisterResponseDto;
import com.mediverse.service.AuthenticationService;
import com.mediverse.service.RegistrationService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {

    private final RegistrationService registrationService;
    private final AuthenticationService authenticationService;

    public AuthController(RegistrationService registrationService, AuthenticationService authenticationService) {
        this.registrationService = registrationService;
        this.authenticationService = authenticationService;
    }

    @PostMapping("/register")
    public ResponseEntity<RegisterResponseDto> registerUser(@Valid @RequestBody RegisterRequestDto request) {
        RegisterResponseDto response = registrationService.registerUser(request);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponseDto> loginUser(@Valid @RequestBody LoginRequestDto request) {
        LoginResponseDto response = authenticationService.login(request);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/refresh")
    public ResponseEntity<RefreshTokenResponseDto> refreshToken(@Valid @RequestBody RefreshTokenRequestDto request) {
        RefreshTokenResponseDto response = authenticationService.refreshToken(request);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/logout")
    public ResponseEntity<MessageResponseDto> logoutUser(
            Authentication authentication,
            @Valid @RequestBody RefreshTokenRequestDto request) {
        String authenticatedUserId = authentication != null ? authentication.getName() : null;
        MessageResponseDto response = authenticationService.logout(authenticatedUserId, request);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/logout-all")
    public ResponseEntity<MessageResponseDto> logoutAll(Authentication authentication) {
        String authenticatedUserId = authentication != null ? authentication.getName() : null;
        MessageResponseDto response = authenticationService.logoutAll(authenticatedUserId);
        return ResponseEntity.ok(response);
    }
}
