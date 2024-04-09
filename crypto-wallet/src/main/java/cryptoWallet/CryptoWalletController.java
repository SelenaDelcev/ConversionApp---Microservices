package cryptoWallet;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;

import org.apache.hc.client5.http.utils.Base64;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/crypto-wallet")
public class CryptoWalletController {

	@Autowired
	private CryptoWalletRepository repo;

	@Autowired
	private BankAccountProxy bankAccountProxy;

	// Vraca crypto wallet usera koji je trenutno ulogovan
	@GetMapping
	public ResponseEntity<?> getCryptoWalletByEmail(@RequestHeader("Authorization") String auth) {
		String pair = new String(Base64.decodeBase64(auth.substring(6)));
		String email = pair.split(":")[0];

		CryptoWallet cryptoWallet = repo.findByEmail(email);

		if (cryptoWallet == null) {
			return ResponseEntity.status(HttpStatus.NOT_FOUND)
					.body("There isn't a crypto wallet tied to that user email in the database.");
		} else {
			CryptoWalletDto cryptoWalletDto = new CryptoWalletDto();

			cryptoWalletDto.setEmail(cryptoWallet.getEmail());
			cryptoWalletDto.setBtc(cryptoWallet.getBtc());
			cryptoWalletDto.setEth(cryptoWallet.getEth());
			cryptoWalletDto.setBnb(cryptoWallet.getBnb());
			cryptoWalletDto.setAda(cryptoWallet.getAda());

			return ResponseEntity.ok(cryptoWalletDto);
		}
	}

	// endpoint koji se poziva iz Users ms kada se kreira novi racun za usera,
	// da mu se kreira crypto wallet
	@PostMapping("/addCryptoWallet")
	public ResponseEntity<?> addUserCryptoWallet(@RequestBody String email) {

		CryptoWallet cryptoWallet = repo.findByEmail(email);

		if (cryptoWallet != null) {
			return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("This user already has a crypto wallet!");
		}
		// Call the Bank Account microservice to check if the user has a bank account
		boolean hasBankAccount = bankAccountProxy.checkBankAccount(email);
		if (!hasBankAccount) {
			return ResponseEntity.status(HttpStatus.BAD_REQUEST)
					.body("Bank account not found for the user. Please create a bank account first.");
		} else {
			Long bankAccountId = ThreadLocalRandom.current().nextLong(3, 101);

			CryptoWallet newUserWallet = new CryptoWallet();

			newUserWallet.setId(bankAccountId);
			newUserWallet.setBtc(new BigDecimal(0));
			newUserWallet.setEth(new BigDecimal(0));
			newUserWallet.setBnb(new BigDecimal(0));
			newUserWallet.setAda(new BigDecimal(0));
			newUserWallet.setEmail(email);

			repo.save(newUserWallet);

			return ResponseEntity.status(HttpStatus.OK).body("Crypto wallet is successfully created.");
		}

	}

	// Menja stanje kripto valuta na racunu
	@PutMapping("/editWallet/{email}")
	public ResponseEntity<?> updateUserCryptoWallet(@RequestBody CryptoWalletUpdateDto userWalletDto,
			@PathVariable("email") String email) {
		CryptoWallet cryptoWallet = repo.findByEmail(email);

		if (cryptoWallet == null) {
			return ResponseEntity.status(HttpStatus.NOT_FOUND)
					.body("There isn't a crypto wallet tied to that user email in the database.");
		} else {
			cryptoWallet.setBtc(userWalletDto.getBtc());
			cryptoWallet.setEth(userWalletDto.getEth());
			cryptoWallet.setBnb(userWalletDto.getBnb());
			cryptoWallet.setAda(userWalletDto.getAda());

			CryptoWallet updatedCryptoWallet = repo.save(cryptoWallet);

			Map<String, Object> responseBody = new HashMap<>();
			responseBody.put("message", "Amounts of currencies on the crypto wallet have been updated successfully!");
			responseBody.put("bank account", mapperCryptoWalletUpdateDto(updatedCryptoWallet));

			return ResponseEntity.ok().body(responseBody);
		}
	}

	// endpoint koji se poziva iz ms CryptoConversion kada dodje do uspesne
	// razmene i transfera novca pa da se promene propagiraju u bazi crypto
	// walleta
	@PutMapping
	public ResponseEntity<?> updateUserAccount(@RequestBody CryptoWalletDto userAccountDto) {
		CryptoWallet userCryptoWallet = repo.findByEmail(userAccountDto.getEmail());

		if (userCryptoWallet == null) {
			return ResponseEntity.status(HttpStatus.NOT_FOUND)
					.body("There isn't a crypto wallet tied to that user email in the database.");
		} else {
			userCryptoWallet.setBtc(userAccountDto.getBtc());
			userCryptoWallet.setEth(userAccountDto.getEth());
			userCryptoWallet.setBnb(userAccountDto.getBnb());
			userCryptoWallet.setAda(userAccountDto.getAda());

			CryptoWallet updatedWallet = repo.save(userCryptoWallet);

			return ResponseEntity.ok(mapperCryptoWalletDto(updatedWallet));
		}
	}

	// endpoint koji se poziva iz ms Users ukoliko dodje do promene mejla usera pa
	// da se promena propagira i na crypto wallet
	@PutMapping("/changeUserEmail/{email}")
	public ResponseEntity<?> updateUserEmail(@RequestBody String newEmail, @PathVariable("email") String oldEmail) {
		CryptoWallet userAccount = repo.findByEmail(oldEmail);

		if (userAccount == null) {
			return ResponseEntity.status(HttpStatus.NOT_FOUND)
					.body("There isn't a crypto wallet tied to that user email in the database.");
		} else {

			userAccount.setEmail(newEmail);

			repo.save(userAccount);

			return ResponseEntity.ok().build();
		}
	}
	
	@PutMapping("/{email}/update/{update}/quantity/{quantity}")
	public CryptoWallet updateWalletAfterTrade(@PathVariable String email, @PathVariable String update, @PathVariable BigDecimal quantity) {
		
		CryptoWallet wallet = repo.findByEmail(email);
		
		if(update.toUpperCase().equals("BTC")) {
			wallet.setBtc(wallet.getBtc().add(quantity));
		}
		else if(update.toUpperCase().equals("ETH")) {
			wallet.setEth(wallet.getEth().add(quantity));
		}
		else if(update.toUpperCase().equals("BNB")) {
			wallet.setBnb(wallet.getBnb().add(quantity));
		}
		else if(update.toUpperCase().equals("ADA")) {
			wallet.setAda(wallet.getAda().add(quantity));
		}
		
		return repo.save(wallet);
	}

	@DeleteMapping("/deleteAccount/{email}")
	public ResponseEntity<?> removeUserCryptoWallet(@PathVariable("email") String email) {
		CryptoWallet userAccount = repo.findByEmail(email);

		if (userAccount == null) {
			return ResponseEntity.status(HttpStatus.NOT_FOUND)
					.body("There isn't a crypto wallet tied to that user email in the database.");
		} else {
			Long userId = userAccount.getId();

			repo.deleteById(userId);

			return ResponseEntity.ok().build();
		}
	}
	
	private CryptoWalletDto mapperCryptoWalletDto(CryptoWallet entity) {
		return new CryptoWalletDto(entity.getEmail(), entity.getBtc(), entity.getEth(), entity.getBnb(), entity.getAda());
	}

	private CryptoWalletUpdateDto mapperCryptoWalletUpdateDto(CryptoWallet entity) {
		return new CryptoWalletUpdateDto(entity.getBtc(), entity.getEth(), entity.getBnb(), entity.getAda());
	}

}
