package com.mediverse.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mediverse.dto.LoginRequestDto;
import com.mediverse.dto.LoginResponseDto;
import com.mediverse.dto.RefreshTokenRequestDto;
import com.mediverse.dto.RegisterRequestDto;
import com.mediverse.entity.AccountStatus;
import com.mediverse.entity.Role;
import com.mediverse.entity.User;
import com.mediverse.repository.RoleRepository;
import com.mediverse.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.WebApplicationContext;

import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(properties = "jwt.secret=mediverse_secure_test_secret_key_1234567890_32bytes!")
@Transactional
class AuthControllerTest {

    @Autowired
    private WebApplicationContext context;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
    }

    @Test
    void test13_InvalidInputRejected() throws Exception {
        MockMvc mockMvc = MockMvcBuilders
                .webAppContextSetup(context)
                .apply(SecurityMockMvcConfigurers.springSecurity())
                .build();

        RegisterRequestDto request = new RegisterRequestDto();
        request.setEmail("invalid-email");
        request.setPassword("short");
        request.setRole("");

        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void test14_PublicRegistrationEndpointSucceedsForPatient() throws Exception {
        MockMvc mockMvc = MockMvcBuilders
                .webAppContextSetup(context)
                .apply(SecurityMockMvcConfigurers.springSecurity())
                .build();

        RegisterRequestDto request = new RegisterRequestDto();
        request.setEmail("api_patient_" + UUID.randomUUID() + "@mediverse.com");
        request.setPassword("SecurePassword123!");
        request.setFullName("API Patient");
        request.setRole("PATIENT");

        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.userId").exists())
                .andExpect(jsonPath("$.role").value("PATIENT"))
                .andExpect(jsonPath("$.accountStatus").value("ACTIVE"))
                .andExpect(jsonPath("$.healthId").exists());
    }

    @Test
    void test15_LoginSucceedsForActivePatientAndReturnsToken() throws Exception {
        MockMvc mockMvc = MockMvcBuilders
                .webAppContextSetup(context)
                .apply(SecurityMockMvcConfigurers.springSecurity())
                .build();

        String email = "api_login_patient_" + UUID.randomUUID() + "@mediverse.com";
        String password = "SecurePassword123!";

        RegisterRequestDto regRequest = new RegisterRequestDto();
        regRequest.setEmail(email);
        regRequest.setPassword(password);
        regRequest.setFullName("Login Patient");
        regRequest.setRole("PATIENT");

        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(regRequest)))
                .andExpect(status().isCreated());

        LoginRequestDto loginRequest = new LoginRequestDto(email, password);

        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").exists())
                .andExpect(jsonPath("$.refreshToken").exists())
                .andExpect(jsonPath("$.tokenType").value("Bearer"))
                .andExpect(jsonPath("$.role").value("PATIENT"))
                .andExpect(jsonPath("$.accountStatus").value("ACTIVE"))
                .andExpect(jsonPath("$.healthId").exists());
    }

    @Test
    void test16_LoginFailsForInvalidCredentialsWith401() throws Exception {
        MockMvc mockMvc = MockMvcBuilders
                .webAppContextSetup(context)
                .apply(SecurityMockMvcConfigurers.springSecurity())
                .build();

        LoginRequestDto loginRequest = new LoginRequestDto("nonexistent@mediverse.com", "WrongPassword123!");

        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").value("Invalid email or password."));
    }

    @Test
    void test17_LoginFailsForPendingDoctorWith403() throws Exception {
        MockMvc mockMvc = MockMvcBuilders
                .webAppContextSetup(context)
                .apply(SecurityMockMvcConfigurers.springSecurity())
                .build();

        String email = "api_pending_doc_" + UUID.randomUUID() + "@mediverse.com";
        String password = "SecurePassword123!";

        RegisterRequestDto regRequest = new RegisterRequestDto();
        regRequest.setEmail(email);
        regRequest.setPassword(password);
        regRequest.setFullName("Pending Doctor");
        regRequest.setRole("DOCTOR");
        regRequest.setLicenseNumber("LIC-12345");

        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(regRequest)))
                .andExpect(status().isCreated());

        LoginRequestDto loginRequest = new LoginRequestDto(email, password);

        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.message").value("Account is pending verification."));
    }

    @Test
    void test18_RefreshEndpointSucceedsWithValidToken() throws Exception {
        MockMvc mockMvc = MockMvcBuilders
                .webAppContextSetup(context)
                .apply(SecurityMockMvcConfigurers.springSecurity())
                .build();

        String email = "api_refresh_patient_" + UUID.randomUUID() + "@mediverse.com";
        String password = "SecurePassword123!";

        RegisterRequestDto regRequest = new RegisterRequestDto();
        regRequest.setEmail(email);
        regRequest.setPassword(password);
        regRequest.setFullName("Refresh Patient");
        regRequest.setRole("PATIENT");

        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(regRequest)))
                .andExpect(status().isCreated());

        LoginRequestDto loginRequest = new LoginRequestDto(email, password);

        String loginResponseStr = mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.refreshToken").exists())
                .andReturn().getResponse().getContentAsString();

        LoginResponseDto loginResponse = objectMapper.readValue(loginResponseStr, LoginResponseDto.class);
        String rawRefreshToken = loginResponse.getRefreshToken();

        RefreshTokenRequestDto refreshRequest = new RefreshTokenRequestDto(rawRefreshToken);

        mockMvc.perform(post("/api/v1/auth/refresh")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(refreshRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").exists())
                .andExpect(jsonPath("$.refreshToken").exists())
                .andExpect(jsonPath("$.tokenType").value("Bearer"))
                .andExpect(jsonPath("$.expiresIn").value(3600));
    }

    @Test
    void test19_RefreshEndpointFailsWithInvalidToken() throws Exception {
        MockMvc mockMvc = MockMvcBuilders
                .webAppContextSetup(context)
                .apply(SecurityMockMvcConfigurers.springSecurity())
                .build();

        RefreshTokenRequestDto refreshRequest = new RefreshTokenRequestDto("invalid_refresh_token_123");

        mockMvc.perform(post("/api/v1/auth/refresh")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(refreshRequest)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void test20_PublicRegistrationRejectedForAdmin() throws Exception {
        MockMvc mockMvc = MockMvcBuilders
                .webAppContextSetup(context)
                .apply(SecurityMockMvcConfigurers.springSecurity())
                .build();

        RegisterRequestDto request = new RegisterRequestDto();
        request.setEmail("admin_" + UUID.randomUUID() + "@mediverse.com");
        request.setPassword("SecurePassword123!");
        request.setFullName("Admin Attempt");
        request.setRole("ADMIN");

        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("ADMIN registration is strictly prohibited."));
    }

    @Test
    void test21_PublicRegistrationRejectedForEmergency() throws Exception {
        MockMvc mockMvc = MockMvcBuilders
                .webAppContextSetup(context)
                .apply(SecurityMockMvcConfigurers.springSecurity())
                .build();

        RegisterRequestDto request = new RegisterRequestDto();
        request.setEmail("emergency_" + UUID.randomUUID() + "@mediverse.com");
        request.setPassword("SecurePassword123!");
        request.setFullName("Emergency Attempt");
        request.setRole("EMERGENCY");

        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("EMERGENCY role registration is not publicly available."));
    }

    @Test
    void test22_LoginFailsForSuspendedAccountWith403() throws Exception {
        MockMvc mockMvc = MockMvcBuilders
                .webAppContextSetup(context)
                .apply(SecurityMockMvcConfigurers.springSecurity())
                .build();

        Role role = roleRepository.findByRoleName("PATIENT").orElseThrow();
        String email = "suspended_" + UUID.randomUUID() + "@mediverse.com";
        User user = new User();
        user.setEmail(email);
        user.setPasswordHash(passwordEncoder.encode("SecurePassword123!"));
        user.setFullName("Suspended Patient");
        user.setRole(role);
        user.setAccountStatus(AccountStatus.SUSPENDED);
        userRepository.save(user);

        LoginRequestDto loginRequest = new LoginRequestDto(email, "SecurePassword123!");

        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.message").value("Account has been suspended."));
    }
}
