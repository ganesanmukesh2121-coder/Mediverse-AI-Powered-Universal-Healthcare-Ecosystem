package com.mediverse.exception;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

class GlobalExceptionHandlerTest {

    private MockMvc mockMvc;

    @RestController
    @RequestMapping("/test-exception")
    static class TestController {

        @GetMapping("/bad-request")
        public void throwBadRequest() {
            throw new IllegalArgumentException("Invalid input parameter");
        }

        @GetMapping("/not-found")
        public void throwNotFound() {
            throw new ResourceNotFoundException("Requested resource was not found");
        }

        @GetMapping("/server-error")
        public void throwServerError() {
            throw new RuntimeException("Unexpected internal failure");
        }
    }

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(new TestController())
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    void handleIllegalArgumentException_ShouldReturn400() throws Exception {
        mockMvc.perform(get("/test-exception/bad-request")
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error").value("Bad Request"))
                .andExpect(jsonPath("$.message").value("Invalid input parameter"))
                .andExpect(jsonPath("$.path").value("/test-exception/bad-request"))
                .andExpect(jsonPath("$.timestamp").exists());
    }

    @Test
    void handleResourceNotFoundException_ShouldReturn404() throws Exception {
        mockMvc.perform(get("/test-exception/not-found")
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.error").value("Not Found"))
                .andExpect(jsonPath("$.message").value("Requested resource was not found"))
                .andExpect(jsonPath("$.path").value("/test-exception/not-found"))
                .andExpect(jsonPath("$.timestamp").exists());
    }

    @Test
    void handleGenericException_ShouldReturn500() throws Exception {
        mockMvc.perform(get("/test-exception/server-error")
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.status").value(500))
                .andExpect(jsonPath("$.error").value("Internal Server Error"))
                .andExpect(jsonPath("$.message").value("Unexpected internal failure"))
                .andExpect(jsonPath("$.path").value("/test-exception/server-error"))
                .andExpect(jsonPath("$.timestamp").exists());
    }
}
