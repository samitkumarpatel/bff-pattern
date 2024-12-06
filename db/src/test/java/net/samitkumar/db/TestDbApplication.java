package net.samitkumar.db;

import org.springframework.boot.SpringApplication;

public class TestDbApplication {

	public static void main(String[] args) {
		SpringApplication.from(DbApplication::main).with(TestcontainersConfiguration.class).run(args);
	}

}
