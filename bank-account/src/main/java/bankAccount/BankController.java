package bankAccount;

import java.util.HashMap;
import java.util.Map;
import java.math.BigDecimal;
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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/bank-account")
public class BankController {

	@Autowired
	private BankAccountRepository repo;

	// Vraca racun usera koji je trenutno ulogovan
	@GetMapping
	public ResponseEntity<?> getUserAccount(@RequestHeader("Authorization") String auth) {
		String pair = new String(Base64.decodeBase64(auth.substring(6)));
		String email = pair.split(":")[0];

		BankAccount user = repo.findByEmail(email);

		if (user == null) {
			return ResponseEntity.status(HttpStatus.NOT_FOUND)
					.body("There isn't a bank account tied to that user email in the database.");
		} else {
			BankAccountDto userAccountDto = new BankAccountDto();

			userAccountDto.setEmail(user.getEmail());
			userAccountDto.setUsd(user.getUsd());
			userAccountDto.setRsd(user.getRsd());
			userAccountDto.setEur(user.getEur());
			userAccountDto.setGbp(user.getGbp());
			userAccountDto.setChf(user.getChf());

			return ResponseEntity.ok(userAccountDto);
		}
	}
	
	//endpoint koji proverava da li user ima racun
	@GetMapping("/check")
	public boolean checkBankAccount(@RequestParam("email") String email) {
	    BankAccount userAccount = repo.findByEmail(email);
	    return userAccount != null;
	}

	// endpoint koji se poziva iz Users ms kada se doda novi user, da mu se kreira racun
	@PostMapping("/addUserAccount")
	public ResponseEntity<?> addUserBankAccount(@RequestBody String email) {
		BankAccount userAccount = repo.findByEmail(email);

		if (userAccount != null) {
			return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("This user already has a bank account!");
		} else {
			Long bankAccountId = ThreadLocalRandom.current().nextLong(3, 101);

			BankAccount newUserAccount = new BankAccount();

			newUserAccount.setId(bankAccountId);
			newUserAccount.setChf(new BigDecimal(0));
			newUserAccount.setRsd(new BigDecimal(0));
			newUserAccount.setEur(new BigDecimal(0));
			newUserAccount.setUsd(new BigDecimal(0));
			newUserAccount.setGbp(new BigDecimal(0));
			newUserAccount.setEmail(email);

			repo.save(newUserAccount);

			return ResponseEntity.ok().build();
		}

	}

	// Menja stanje valuta na racunu
	@PutMapping("/editAccount/{email}")
	public ResponseEntity<?> updateUserBankAccount(@RequestBody BankAccountUpdateDto userAccountDto,
			@PathVariable("email") String email) {
		BankAccount bankAccount = repo.findByEmail(email);

		if (bankAccount == null) {
			return ResponseEntity.status(HttpStatus.NOT_FOUND)
					.body("There isn't a bank account tied to that user email in the database.");
		} else {
			bankAccount.setRsd(userAccountDto.getRsd());
			bankAccount.setEur(userAccountDto.getEur());
			bankAccount.setUsd(userAccountDto.getUsd());
			bankAccount.setGbp(userAccountDto.getGbp());
			bankAccount.setChf(userAccountDto.getChf());

			BankAccount updatedBankAccount = repo.save(bankAccount);

			Map<String, Object> responseBody = new HashMap<>();
			responseBody.put("message", "Amounts of currencies on the bank account have been updated successfully!");
			responseBody.put("bank account", mapperBankAccountUpdateDto(updatedBankAccount));

			return ResponseEntity.ok().body(responseBody);
		}
	}

	// endpoint koji se poziva iz ms CurrencyConversion kada dodje do uspesne
	// razmene i transfera novca pa da se promene propagiraju u bazi bankovnog
	// racuna
	@PutMapping
	public ResponseEntity<?> updateUserAccount(@RequestBody BankAccountDto userAccountDto) {
		BankAccount userAccount = repo.findByEmail(userAccountDto.getEmail());

		if (userAccount == null) {
			return ResponseEntity.status(HttpStatus.NOT_FOUND)
					.body("There isn't a bank account tied to that user email in the database.");
		} else {
			userAccount.setRsd(userAccountDto.getRsd());
			userAccount.setEur(userAccountDto.getEur());
			userAccount.setUsd(userAccountDto.getUsd());
			userAccount.setGbp(userAccountDto.getGbp());
			userAccount.setChf(userAccountDto.getChf());

			BankAccount updatedAccount = repo.save(userAccount);

			return ResponseEntity.ok(mapperBankAccountDto(updatedAccount));
		}
	}

	// endpoint koji se poziva iz ms Users ukoliko dodje do promene mejla usera pa
	// da se promena propagira i na bankovnom racunu
	@PutMapping("/changeUserEmail/{email}")
	public ResponseEntity<?> updateUserEmail(@RequestBody String newEmail, @PathVariable("email") String oldEmail) {
		BankAccount userAccount = repo.findByEmail(oldEmail);

		if (userAccount == null) {
			return ResponseEntity.status(HttpStatus.NOT_FOUND)
					.body("There isn't a bank account tied to that user email in the database.");
		} else {

			userAccount.setEmail(newEmail);

			repo.save(userAccount);

			return ResponseEntity.ok().build();
		}
	}
	
	@PutMapping("/{email}/update/{update}/quantity/{quantity}")
	public BankAccount updateAccountAfterTrade(@PathVariable String email, @PathVariable String update, @PathVariable BigDecimal quantity) {
		
		BankAccount bankAccount = repo.findByEmail(email);
		
		if(update.toUpperCase().equals("USD")) {
			bankAccount.setUsd(bankAccount.getUsd().add(quantity));
		}
		else if(update.toUpperCase().equals("GBP")) {
			bankAccount.setGbp(bankAccount.getGbp().add(quantity));
		}
		else if(update.toUpperCase().equals("CHF")) {
			bankAccount.setChf(bankAccount.getChf().add(quantity));
		}
		else if(update.toUpperCase().equals("EUR")) {
			bankAccount.setEur(bankAccount.getEur().add(quantity));
		}
		else if(update.toUpperCase().equals("RSD")) {
			bankAccount.setRsd(bankAccount.getRsd().add(quantity));
		}
				
		return repo.save(bankAccount);
	}

	@DeleteMapping("/deleteAccount/{email}")
	public ResponseEntity<?> removeUserBankAccount(@PathVariable("email") String email) {
		BankAccount userAccount = repo.findByEmail(email);

		if (userAccount == null) {
			return ResponseEntity.status(HttpStatus.NOT_FOUND)
					.body("There isn't a bank account tied to that user email in the database.");
		} else {
			Long userId = userAccount.getId();

			repo.deleteById(userId);

			return ResponseEntity.ok().build();
		}
	}

	private BankAccountDto mapperBankAccountDto(BankAccount entity) {
		return new BankAccountDto(entity.getEmail(), entity.getUsd(), entity.getRsd(), entity.getEur(), entity.getGbp(),
				entity.getChf());
	}

	private BankAccountUpdateDto mapperBankAccountUpdateDto(BankAccount entity) {
		return new BankAccountUpdateDto(entity.getUsd(), entity.getRsd(), entity.getEur(), entity.getGbp(),
				entity.getChf());
	}

}
