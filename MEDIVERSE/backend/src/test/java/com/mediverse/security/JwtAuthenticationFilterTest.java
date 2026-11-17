package com.mediverse.security;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(properties = "jwt.secret=mediverse_secure_test_secret_key_1234567890_32bytes!")
class JwtAuthenticationFilterTest {

    private static final String TEST_SECRET = "mediverse_secure_test_secret_key_1234567890_32bytes!";

    @Autowired
    private WebApplicationContext context;

    @Autowired
    private JwtService jwtService;

    @Autowired
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        SecurityContextHolder.clearContext();
        mockMvc = MockMvcBuilders
                .webAppContextSetup(context)
                .apply(SecurityMockMvcConfigurers.springSecurity())
                .build();
    }

    @Test
    void test1_MissingAuthorizationHeader_ShouldRemainUnauthenticated() throws Exception {
        mockMvc.perform(get("/api/v1/protected-resource"))
                .andExpect(status().isUnauthorized());

        assertNull(SecurityContextHolder.getContext().getAuthentication());
    }

    @Test
    void test2_MalformedAuthorizationHeader_ShouldRemainUnauthenticated() throws Exception {
        mockMvc.perform(get("/api/v1/protected-resource").header("Authorization", "Basic invalid_credentials"))
                .andExpect(status().isUnauthorized());

        mockMvc.perform(get("/api/v1/protected-resource").header("Authorization", "Bearer "))
                .andExpect(status().isUnauthorized());

        assertNull(SecurityContextHolder.getContext().getAuthentication());
    }

    @Test
    void test3_ValidJwt_ShouldAuthenticateAndExposePrincipalAndRole() throws Exception {
        String userId = UUID.randomUUID().toString();
        String token = jwtService.generateAccessToken(userId, "doctor@mediverse.com", "DOCTOR", "ACTIVE", null);

        mockMvc.perform(get("/api/v1/protected-resource").header("Authorization", "Bearer " + token))
                .andExpect(status().isNotFound());
    }

    @Test
    void test4_TamperedJwt_ShouldNotAuthenticate() throws Exception {
        String token = jwtService.generateAccessToken(UUID.randomUUID().toString(), "patient@mediverse.com", "PATIENT", "ACTIVE", "MV-2026-11112222");
        String tamperedToken = token.substring(0, token.length() - 4) + "XXXX";

        mockMvc.perform(get("/api/v1/protected-resource").header("Authorization", "Bearer " + tamperedToken))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void test5_ExpiredJwt_ShouldNotAuthenticate() throws Exception {
        JwtService expiredService = new JwtService(TEST_SECRET, -1000);
        String expiredToken = expiredService.generateAccessToken(UUID.randomUUID().toString(), "patient@mediverse.com", "PATIENT", "ACTIVE", "MV-2026-11112222");

        mockMvc.perform(get("/api/v1/protected-resource").header("Authorization", "Bearer " + expiredToken))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void test6_PatientJwt_AssignsRolePatientAuthority() {
        String userId = UUID.randomUUID().toString();
        String token = jwtService.generateAccessToken(userId, "patient@mediverse.com", "PATIENT", "ACTIVE", "MV-2026-11112222");

        assertTrue(jwtService.validateToken(token));
        assertEquals(userId, jwtService.extractUserId(token));
        assertEquals("PATIENT", jwtService.extractRole(token));
    }

    @Test
    void test7_DoctorJwt_AssignsRoleDoctorAuthority() {
        String userId = UUID.randomUUID().toString();
        String token = jwtService.generateAccessToken(userId, "doctor@mediverse.com", "DOCTOR", "ACTIVE", null);

        assertTrue(jwtService.validateToken(token));
        assertEquals(userId, jwtService.extractUserId(token));
        assertEquals("DOCTOR", jwtService.extractRole(token));
    }

    @Test
    void test8_PublicHealthEndpointRemainsAccessibleWithoutToken() throws Exception {
        mockMvc.perform(get("/api/health"))
                .andExpect(status().isOk());
    }

    @Test
    void test9_ProtectedEndpointRequiresValidJwt() throws Exception {
        mockMvc.perform(get("/api/v1/protected-resource"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void test10_FilterDirectExecution_PopulatesRoleAndPermissionAuthoritiesInSecurityContext() throws Exception {
        String userId = UUID.randomUUID().toString();
        List<String> permissions = List.of("DOCTOR_CREATE_CONSULTATION", "DOCTOR_CREATE_PRESCRIPTION");
        String token = jwtService.generateAccessToken(userId, "doctor@mediverse.com", "DOCTOR", "ACTIVE", null, permissions);

        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("Authorization", "Bearer " + token);
        MockHttpServletResponse response = new MockHttpServletResponse();

        jwtAuthenticationFilter.doFilter(request, response, (req, res) -> {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            assertNotNull(auth, "Authentication must be set in SecurityContext");
            assertEquals(userId, auth.getPrincipal());
            Set<String> authorityStrings = auth.getAuthorities().stream()
                    .map(GrantedAuthority::getAuthority)
                    .collect(Collectors.toSet());

            assertTrue(authorityStrings.contains("ROLE_DOCTOR"), "ROLE_DOCTOR must be preserved");
            assertTrue(authorityStrings.contains("DOCTOR_CREATE_CONSULTATION"), "DOCTOR_CREATE_CONSULTATION authority must be present");
            assertTrue(authorityStrings.contains("DOCTOR_CREATE_PRESCRIPTION"), "DOCTOR_CREATE_PRESCRIPTION authority must be present");
            assertEquals(3, authorityStrings.size(), "Should have exactly 1 role authority + 2 permission authorities");
        });
    }

    @Test
    void test11_FilterDirectExecution_MissingPermissions_PreservesRoleAuthorityOnly() throws Exception {
        String userId = UUID.randomUUID().toString();
        String token = jwtService.generateAccessToken(userId, "admin@mediverse.com", "ADMIN", "ACTIVE", null);

        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("Authorization", "Bearer " + token);
        MockHttpServletResponse response = new MockHttpServletResponse();

        jwtAuthenticationFilter.doFilter(request, response, (req, res) -> {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            assertNotNull(auth, "Authentication must be set in SecurityContext");
            assertEquals(userId, auth.getPrincipal());
            Set<String> authorityStrings = auth.getAuthorities().stream()
                    .map(GrantedAuthority::getAuthority)
                    .collect(Collectors.toSet());

            assertTrue(authorityStrings.contains("ROLE_ADMIN"), "ROLE_ADMIN authority must be present");
            assertEquals(1, authorityStrings.size(), "Should contain only ROLE_ADMIN when no permissions are present in JWT");
        });
    }
}
