package usersService;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;

@FeignClient(name="bank-account")
public interface BankAccountProxy {
	
	@GetMapping("/bank-account")
	public ResponseEntity<?> getUserAccount(@RequestHeader("Authorization") String auth);
	
	@PostMapping("/bank-account/addUserAccount")
	public ResponseEntity<?> addUserBankAccount(@RequestBody String email);
	
	@DeleteMapping("/bank-account/deleteAccount/{email}")
	public ResponseEntity<?> removeUserBankAccount(@PathVariable String email);
	
	@PutMapping("/bank-account/changeUserEmail/{email}")
	public ResponseEntity<?> updateUserEmail(@RequestBody String newEmail, @PathVariable("email") String oldEmail);
}
