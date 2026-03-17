package com.almirdev.shortr;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;

@Import(TestcontainersConfiguration.class)
@SpringBootTest
class ShortrApplicationTests {

	@Test
	void contextLoads() {
	}

}
