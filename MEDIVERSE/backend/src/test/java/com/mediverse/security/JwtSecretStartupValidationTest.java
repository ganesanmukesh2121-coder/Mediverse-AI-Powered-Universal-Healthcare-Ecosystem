package com.mediverse.security;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;

import static org.assertj.core.api.Assertions.assertThat;

class JwtSecretStartupValidationTest {

    private final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
            .withUserConfiguration(JwtService.class);

    @Test
    @DisplayName("Application context fails startup when JWT secret is missing")
    void missingJwtSecretPreventsStartup() {
        contextRunner.run(context -> {
            assertThat(context).hasFailed();
            assertThat(context.getStartupFailure())
                    .hasRootCauseInstanceOf(IllegalStateException.class)
                    .hasRootCauseMessage("JWT secret configuration is missing or blank. Set JWT_SECRET environment variable or jwt.secret property.");
        });
    }

    @Test
    @DisplayName("Application context fails startup when JWT secret is blank")
    void blankJwtSecretPreventsStartup() {
        contextRunner
                .withPropertyValues("jwt.secret=   ")
                .run(context -> {
                    assertThat(context).hasFailed();
                    assertThat(context.getStartupFailure())
                            .hasRootCauseInstanceOf(IllegalStateException.class)
                            .hasRootCauseMessage("JWT secret configuration is missing or blank. Set JWT_SECRET environment variable or jwt.secret property.");
                });
    }

    @Test
    @DisplayName("Application context fails startup when JWT secret is shorter than 256 bits (32 bytes)")
    void shortJwtSecretPreventsStartup() {
        String shortSecret = "under_32_characters_secret";
        contextRunner
                .withPropertyValues("jwt.secret=" + shortSecret)
                .run(context -> {
                    assertThat(context).hasFailed();
                    assertThat(context.getStartupFailure())
                            .hasRootCauseInstanceOf(IllegalStateException.class)
                            .hasRootCauseMessage("JWT secret key must be at least 256 bits (32 bytes) for HS256 signing.");
                    // Verify the exception message does not leak the secret value
                    assertThat(context.getStartupFailure().getMessage()).doesNotContain(shortSecret);
                });
    }

    @Test
    @DisplayName("Application context starts successfully when valid JWT secret is provided via jwt.secret")
    void validJwtSecretViaPropertyStartsSuccessfully() {
        contextRunner
                .withPropertyValues("jwt.secret=mediverse_secure_test_secret_key_1234567890_32bytes!")
                .run(context -> {
                    assertThat(context).hasNotFailed();
                    assertThat(context).hasSingleBean(JwtService.class);
                });
    }

    @Test
    @DisplayName("Application context starts successfully when valid JWT secret is provided via JWT_SECRET environment variable")
    void validJwtSecretViaEnvStartsSuccessfully() {
        contextRunner
                .withPropertyValues("JWT_SECRET=mediverse_secure_test_secret_key_1234567890_32bytes!")
                .run(context -> {
                    assertThat(context).hasNotFailed();
                    assertThat(context).hasSingleBean(JwtService.class);
                });
    }
}
