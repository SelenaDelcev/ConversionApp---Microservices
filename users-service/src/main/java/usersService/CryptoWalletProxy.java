package usersService;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(name = "crypto-wallet")
public interface CryptoWalletProxy {
	
	@PostMapping("/crypto-wallet/addCryptoWallet")
	public ResponseEntity<?> addUserCryptoWallet(@RequestBody String email);
	
	@DeleteMapping("/crypto-wallet/deleteAccount/{email}")
	public ResponseEntity<?> removeUserCryptoWallet(@PathVariable String email);
	
	@PutMapping("/crypto-wallet/changeUserEmail/{email}")
	public ResponseEntity<?> updateUserEmail(@RequestBody String newEmail, @PathVariable("email") String oldEmail);

}

