package com.buyology.backend;

import com.buyology.backend.config.InfisicalPropertySource;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;

@SpringBootTest
@ContextConfiguration(initializers = InfisicalPropertySource.class)
class SbEcomApplicationTests {

	@Test
	void contextLoads() {
	}

}
