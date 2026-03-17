package com.almirdev.shortr;

import org.springframework.boot.SpringApplication;

public class TestShortrApplication {

	public static void main(String[] args) {
		SpringApplication.from(ShortrApplication::main).with(TestcontainersConfiguration.class).run(args);
	}

}
