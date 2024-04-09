package cryptoConversion;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;


@FeignClient(name="crypto-wallet")
public interface CryptoWalletProxy {
	
	@GetMapping("/crypto-wallet")
	public CryptoWalletDto getUserAccount(@RequestHeader("Authorization") String auth);
	
	@PostMapping("/crypto-wallet/addUserAccount")
	public ResponseEntity<?> addUserBankAccount(@RequestBody String email);
	
	@DeleteMapping("/crypto-wallet/deleteAccount/{email}")
	public ResponseEntity<?> removeUserBankAccount(@PathVariable String email);
	
	@PutMapping("/crypto-wallet")
	ResponseEntity<CryptoWalletDto> updateUserAccount(@RequestBody CryptoWalletDto userDto);
}
