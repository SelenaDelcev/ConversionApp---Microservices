package currencyConversion;

import java.math.BigDecimal;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

@Service
public class CurrencyConversionService {
	
	@Autowired
	private CurrencyExchangeProxy currencyExchangeProxy;
	
	@Autowired
	private BankAccountProxy bankAccountProxy;
	
	public ResponseEntity<MessageDto<String, BankAccountDto>> getConversion(String from, String to, BigDecimal quantity, String auth) {
		CurrencyConversion exchangeRate = currencyExchangeProxy.getExchange(from, to);
		
		if (exchangeRate == null) {
			MessageDto<String, BankAccountDto> messageDto = new MessageDto<>("There was a problem with the currency exchange service", null);
			return ResponseEntity.status(HttpStatus.CONFLICT).body(messageDto);
		}
		
		BankAccountDto userAccount = bankAccountProxy.getUserAccount(auth);
		
		if (!checkBalance(userAccount, from.toUpperCase(), quantity)) { 
			MessageDto<String, BankAccountDto> messageDto = new MessageDto<>("The balance on the account is not enough", null);
			return ResponseEntity.status(HttpStatus.NOT_ACCEPTABLE).body(messageDto);
		}
		
		userAccount = updateBalance(userAccount, exchangeRate, quantity);
		
		ResponseEntity<?> response = bankAccountProxy.updateUserAccount(userAccount);
		
		if(response.getStatusCode() == HttpStatus.OK) {
			MessageDto<String, BankAccountDto> messageDto = new MessageDto<>("Conversion and transfer successful for the amount of " + quantity + " " + from + " to " 
						+ quantity.multiply(exchangeRate.getConversionMultiple()) + " " + to, userAccount);
			return ResponseEntity.status(HttpStatus.OK).body(messageDto);
		} else {
			MessageDto<String, BankAccountDto> messageDto = new MessageDto<>("Failed to update user's bank account", null);
			return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(messageDto);
		}
		
	}
	
	private boolean isBalanceGreater(BigDecimal balance, BigDecimal quantity) {
		return balance.compareTo(quantity) >= 0;
	}
	
	private boolean checkBalance(BankAccountDto userAccount, String from, BigDecimal quantity) {
		switch (from) {
			case "RSD":
				return isBalanceGreater(userAccount.getRsd(), quantity); 
			case "USD":
				return isBalanceGreater(userAccount.getUsd(), quantity); 
			case "EUR":
				return isBalanceGreater(userAccount.getEur(), quantity);
			case "GBP":
				return isBalanceGreater(userAccount.getGbp(), quantity); 
			case "CHF":
				return isBalanceGreater(userAccount.getChf(), quantity); 
			default:
				return false;
		}
	}
	
	private BankAccountDto updateBalance(BankAccountDto userAccount, CurrencyConversion exchangeRate, BigDecimal quantity) {
		BigDecimal amount = quantity.multiply(exchangeRate.getConversionMultiple());;
		
		switch (exchangeRate.getFrom()) {
			case "RSD":
				
				switch (exchangeRate.getTo()) {
					case "USD":
						userAccount.setRsd(userAccount.getRsd().subtract(quantity));
						userAccount.setUsd(userAccount.getUsd().add(amount));
						
						break;
					case "EUR":
						userAccount.setRsd(userAccount.getRsd().subtract(quantity));
						userAccount.setEur(userAccount.getEur().add(amount));
						
						break;
					case "GBP":
						userAccount.setRsd(userAccount.getRsd().subtract(quantity));
						userAccount.setGbp(userAccount.getGbp().add(amount));
						
						break;
					case "CHF":
						userAccount.setRsd(userAccount.getRsd().subtract(quantity));
						userAccount.setChf(userAccount.getChf().add(amount));
						
						break;
				}
				
				break;
			case "EUR":
				
				switch (exchangeRate.getTo()) {
					case "RSD":
						userAccount.setEur(userAccount.getEur().subtract(quantity));
						userAccount.setRsd(userAccount.getRsd().add(amount));
						
						break;
					case "USD":
						userAccount.setEur(userAccount.getEur().subtract(quantity));
						userAccount.setUsd(userAccount.getUsd().add(amount));
						
						break;
					case "GBP":
						userAccount.setEur(userAccount.getEur().subtract(quantity));
						userAccount.setGbp(userAccount.getGbp().add(amount));
						
						break;
					case "CHF":
						userAccount.setEur(userAccount.getEur().subtract(quantity));
						userAccount.setChf(userAccount.getChf().add(amount));
						
						break;
				}
				
				break;
			case "USD":
				
				switch (exchangeRate.getTo()) {
					case "RSD":
						userAccount.setUsd(userAccount.getUsd().subtract(quantity));
						userAccount.setRsd(userAccount.getRsd().add(amount));
						
						break;
					case "EUR":
						userAccount.setUsd(userAccount.getUsd().subtract(quantity));
						userAccount.setEur(userAccount.getEur().add(amount));
						
						break;
					case "GBP":
						userAccount.setUsd(userAccount.getUsd().subtract(quantity));
						userAccount.setGbp(userAccount.getGbp().add(amount));
						
						break;
					case "CHF":
						userAccount.setUsd(userAccount.getUsd().subtract(quantity));
						userAccount.setChf(userAccount.getChf().add(amount));
						
						break;
				}
				
				break;
			case "GBP":
				switch (exchangeRate.getTo()) {
					case "USD":
						userAccount.setGbp(userAccount.getGbp().subtract(quantity));
						userAccount.setUsd(userAccount.getUsd().add(amount));
						
						break;
					case "RSD":
						userAccount.setGbp(userAccount.getGbp().subtract(quantity));
						userAccount.setRsd(userAccount.getRsd().add(amount));
						
						break;
					case "EUR":
						userAccount.setGbp(userAccount.getGbp().subtract(quantity));
						userAccount.setEur(userAccount.getEur().add(amount));
						
						break;
					case "CHF":
						userAccount.setGbp(userAccount.getGbp().subtract(quantity));
						userAccount.setChf(userAccount.getChf().add(amount));
						
						break;
				}
				
				break;
			case "CHF":
				switch (exchangeRate.getTo()) {
					case "USD":
						userAccount.setChf(userAccount.getChf().subtract(quantity));
						userAccount.setUsd(userAccount.getUsd().add(amount));
						
						break;
					case "RSD":
						userAccount.setChf(userAccount.getChf().subtract(quantity));
						userAccount.setRsd(userAccount.getRsd().add(amount));
						
						break;
					case "EUR":
						userAccount.setChf(userAccount.getChf().subtract(quantity));
						userAccount.setEur(userAccount.getEur().add(amount));
						
						break;
					case "GBP":
						userAccount.setChf(userAccount.getChf().subtract(quantity));
						userAccount.setGbp(userAccount.getGbp().add(amount));
						
						break;
				}
				
				break;
		}
		
		return userAccount;
	}
}
