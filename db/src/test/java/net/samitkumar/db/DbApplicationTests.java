package net.samitkumar.db;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;

import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertAll;

@Import(TestcontainersConfiguration.class)
@SpringBootTest
class DbApplicationTests {

	@Test
	void contextLoads() {
	}

	@Test
	void entityTest(@Autowired UserRepository userRepository) {
		assertAll(
				//all
				() -> userRepository
						.findAll()
						.forEach(System.out::println),
				//add multiple
				() -> userRepository
						.saveAll(
							List.of(
								new User(null, "uOne", "uOnePassword", true, null,
										Set.of(
											new Roles(null, Role.READ)
										)
								),
								new User(null, "uTwo", "uTwoPassword", true, null,
										Set.of(
											new Roles(null, Role.READ),
											new Roles(null, Role.WRITE)
										)
								),
								new User(null, "uThree", "uThreePassword", false, null,
										Set.of(
											new Roles(null, Role.ADMIN)
										)
								)
							)
						)
						.forEach(System.out::println),
				//update one
				() -> userRepository.findById(11L)
						.ifPresentOrElse(u ->
								userRepository.save(new User(u.id(), u.username(), u.password(), false, null, u.roles())),
								UserNotFoundException::new),
				//all
				() -> {
					System.out.println("#################################");
					userRepository
						.findAll()
						.forEach(System.out::println);
				}
		);
	}

}
