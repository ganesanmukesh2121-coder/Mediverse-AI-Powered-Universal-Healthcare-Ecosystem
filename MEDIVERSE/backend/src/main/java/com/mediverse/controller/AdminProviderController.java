package com.mediverse.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.mediverse.dto.PendingProviderResponseDto;
import com.mediverse.dto.ProviderVerificationRequestDto;
import com.mediverse.service.ProviderVerificationService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/v1/admin/providers")
public class AdminProviderController {

    private final ProviderVerificationService providerVerificationService;

    public AdminProviderController(ProviderVerificationService providerVerificationService) {
        this.providerVerificationService = providerVerificationService;
    }

    @GetMapping("/pending")
    @PreAuthorize("hasAuthority('ADMIN_VERIFY_PROVIDER')")
    public ResponseEntity<List<PendingProviderResponseDto>> getPendingProviders() {
        List<PendingProviderResponseDto> pending = providerVerificationService.getPendingProviders();
        return ResponseEntity.ok(pending);
    }

    @PutMapping("/{userId}/verify")
    @PreAuthorize("hasAuthority('ADMIN_VERIFY_PROVIDER')")
    public ResponseEntity<PendingProviderResponseDto> verifyProvider(
            @PathVariable("userId") String userId,
            @Valid @RequestBody ProviderVerificationRequestDto request,
            Authentication authentication) {
        String adminUserId = authentication != null ? authentication.getName() : null;
        PendingProviderResponseDto response = providerVerificationService.verifyProvider(userId, request, adminUserId);
        return ResponseEntity.ok(response);
    }
}
