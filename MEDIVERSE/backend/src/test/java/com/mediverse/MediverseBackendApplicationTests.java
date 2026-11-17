package com.mediverse;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest(properties = "jwt.secret=mediverse_secure_test_secret_key_1234567890_32bytes!")
class MediverseBackendApplicationTests {

	@Test
	void contextLoads() {
	}

}
