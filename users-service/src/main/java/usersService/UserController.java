package usersService;

import java.util.HashMap;
import java.util.Map;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

import org.springframework.http.HttpStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import usersService.model.CustomUser;

@RestController
@RequestMapping("/users-service/users")
public class UserController {

	@Autowired
	private CustomUserRepository repo;

	@Autowired
	private BankAccountProxy bankAccountProxy;

	@Autowired
	private CryptoWalletProxy cryptoWalletProxy;

	@GetMapping()
	public List<CustomUser> getAllUsers() {
		List<CustomUser> users = repo.findAll();

		return users;
	}

	@PostMapping("/addUser")
	public ResponseEntity<?> createUser(@RequestBody CreateUserDto newUserDto) {
		CustomUser user = repo.findByEmail(newUserDto.getEmail());

		if (user != null) {
			return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("User already exists in the database");
		} else {

			if (newUserDto.getEmail() == null || newUserDto.getPassword() == null) {
				return ResponseEntity.status(400).body("Email and password are required.");
			}

			Long userId = ThreadLocalRandom.current().nextLong(5, 101);

			CustomUser newUser = new CustomUser();

			newUser.setId(userId);
			newUser.setEmail(newUserDto.getEmail());
			newUser.setPassword(newUserDto.getPassword());
			newUser.setRole("USER");

			repo.save(newUser);

			ResponseEntity<?> response = bankAccountProxy.addUserBankAccount(newUserDto.getEmail());

			if (response.getStatusCode() == HttpStatus.OK) {
				ResponseEntity<?> cryptoWalletResponse = cryptoWalletProxy.addUserCryptoWallet(newUserDto.getEmail());

				if (cryptoWalletResponse.getStatusCode() == HttpStatus.OK) {
					Map<String, Object> responseBody = new HashMap<>();
					responseBody.put("message",
							"User, bank account, and crypto wallet have been created successfully!");
					responseBody.put("user", mapperUserDto(newUser));

					return ResponseEntity.status(201).body(responseBody);
				} else {
					return ResponseEntity.status(500).body("Something went wrong while creating a new crypto wallet.");
				}
			} else {
				return ResponseEntity.status(500).body("Something went wrong while creating a new bank account.");
			}
		}

	}

	@PostMapping("/addUserOrAdmin")
	public ResponseEntity<?> createUserOrAdmin(@RequestBody CreateAdminOrUserDto newUserAdminDto) {
		CustomUser user = repo.findByEmail(newUserAdminDto.getEmail());

		if (user != null) {
			return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("User already exists in the database");
		} else {

			if (newUserAdminDto.getEmail() == null || newUserAdminDto.getPassword() == null
					|| newUserAdminDto.getRole() == null) {
				return ResponseEntity.status(400).body("Email, password and role are required.");
			}

			if (newUserAdminDto.getRole().toString().equals("OWNER")) {
				return ResponseEntity.status(HttpStatus.BAD_REQUEST)
						.body("There's already a user with a role OWNER in the database. There can only be one!");
			} else if (newUserAdminDto.getRole().toString().equals("USER")) {
				Long userId = ThreadLocalRandom.current().nextLong(5, 101);

				CustomUser newUser = new CustomUser();

				newUser.setId(userId);
				newUser.setEmail(newUserAdminDto.getEmail());
				newUser.setPassword(newUserAdminDto.getPassword());
				newUser.setRole(newUserAdminDto.getRole());

				repo.save(newUser);

				ResponseEntity<?> response = bankAccountProxy.addUserBankAccount(newUserAdminDto.getEmail());

				if (response.getStatusCode() == HttpStatus.OK) {
					ResponseEntity<?> cryptoWalletResponse = cryptoWalletProxy.addUserCryptoWallet(newUserAdminDto.getEmail());

					if (cryptoWalletResponse.getStatusCode() == HttpStatus.OK) {
						Map<String, Object> responseBody = new HashMap<>();
						responseBody.put("message",
								"User, bank account, and crypto wallet have been created successfully!");
						responseBody.put("user", mapperUserDto(newUser));

						return ResponseEntity.status(201).body(responseBody);
					} else {
						return ResponseEntity.status(500).body("Something went wrong while creating a new crypto wallet.");
					}
				} else {
					return ResponseEntity.status(500).body("Something went wrong while creating a new bank account.");
				}
			} else if (newUserAdminDto.getRole().toString().equals("ADMIN")) {
				Long userId = ThreadLocalRandom.current().nextLong(5, 101);

				CustomUser newUser = new CustomUser();

				newUser.setId(userId);
				newUser.setEmail(newUserAdminDto.getEmail());
				newUser.setPassword(newUserAdminDto.getPassword());
				newUser.setRole(newUserAdminDto.getRole());

				repo.save(newUser);

				Map<String, Object> responseBody = new HashMap<>();
				responseBody.put("message", "Admin has been created successfully!");
				responseBody.put("user", mapperUserDto(newUser));

				return ResponseEntity.status(201).body(responseBody);
			} else {
				return ResponseEntity.status(400).body("Role must be USER or ADMIN.");
			}
		}
	}

	@PutMapping("/updateUser/{email}")
	public ResponseEntity<?> updateUser(@RequestBody UpdateDto updateUserDto,
			@PathVariable("email") String emailToChange) {
		CustomUser user = repo.findByEmail(emailToChange);

		if (user == null) {
			return ResponseEntity.status(HttpStatus.NOT_FOUND).body("No user with such email found in the database");
		}

		if (updateUserDto.getEmail() == null || updateUserDto.getPassword() == null) {
			return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Email and password are required.");
		}

		if (!user.getRole().equals("USER")) {
			return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Only users can be updated by this endpoint.");
		}

		// Check if the email is changed
		boolean emailChanged = !user.getEmail().equals(updateUserDto.getEmail());

		// Check if the password is changed
		boolean passwordChanged = !user.getPassword().equals(updateUserDto.getPassword());

		user.setEmail(updateUserDto.getEmail());
		user.setPassword(updateUserDto.getPassword());

		repo.save(user);

		ResponseEntity<?> response = null;

		// Call the Bank Account Proxy only if the email is changed
		if (emailChanged) {
			response = bankAccountProxy.updateUserEmail(updateUserDto.getEmail(), emailToChange);

			if (response.getStatusCode() != HttpStatus.OK) {
				return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
						.body("Something went wrong while updating the user's email for their bank account.");
			}
		}

		// Update user data if the password is changed
		if (passwordChanged) {

			repo.save(user);

			return ResponseEntity.status(HttpStatus.OK).body("Password updated successfully!");
		}

		Map<String, Object> responseBody = new HashMap<>();
		responseBody.put("message", "User data updated successfully!");
		responseBody.put("user", mapperUserUpdateDto(user));

		return ResponseEntity.status(HttpStatus.OK).body(responseBody);
	}

	@PutMapping("/updateAnyone/{email}")
	public ResponseEntity<?> updateAnyone(@RequestBody UpdateDto updateDto,
			@PathVariable("email") String emailToChange) {
		CustomUser user = repo.findByEmail(emailToChange);

		if (user == null) {
			return ResponseEntity.status(HttpStatus.NOT_FOUND).body("No user with such email found in the database");
		} else {
			if (updateDto.getEmail() == null || updateDto.getPassword() == null) {
				return ResponseEntity.status(400).body("Email and password are required.");
			}

			user.setEmail(updateDto.getEmail());
			user.setPassword(updateDto.getPassword());

			CustomUser updatedUser = repo.save(user);

			Map<String, Object> responseBody = new HashMap<>();
			responseBody.put("message", "User data updated successfully!");
			responseBody.put("user", mapperUserUpdateDto(updatedUser));

			return ResponseEntity.status(200).body(responseBody);
		}
	}

	@DeleteMapping("removeUser/{email}")
	public ResponseEntity<?> removeUser(@PathVariable("email") String email) {
		CustomUser user = repo.findByEmail(email);

		if (user == null) {
			return ResponseEntity.status(HttpStatus.NOT_FOUND).body("No user with such email found in the database.");
		} else {
			String role = user.getRole();

			Long userId = user.getId();

			if (role.toString().equals("USER")) {
				repo.deleteById(userId);

				ResponseEntity<?> response = bankAccountProxy.removeUserBankAccount(email);

				if (response.getStatusCode() == HttpStatus.OK) {
					ResponseEntity<?> cryptoWalletResponse = cryptoWalletProxy.removeUserCryptoWallet(email);

					if (cryptoWalletResponse.getStatusCode() == HttpStatus.OK) {

						return ResponseEntity.status(204).body("User and his bank account and crypto wallet is deleted successfully.");
					} else {
						return ResponseEntity.status(500).body("Something went wrong while creating a new crypto wallet.");
					}
				} else {
					return ResponseEntity.status(500).body("Something went wrong while creating a new bank account.");
				}
			} else {
				repo.deleteById(userId);

				return ResponseEntity.status(200).body(role + " deleted successfully!");
			}
		}
	}

	private UserDto mapperUserDto(CustomUser user) {
		return new UserDto(user.getEmail(), user.getPassword(), user.getRole());
	}

	private UpdateDto mapperUserUpdateDto(CustomUser user) {
		return new UpdateDto(user.getEmail(), user.getPassword());
	}

}
