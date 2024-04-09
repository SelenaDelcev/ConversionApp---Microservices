package cryptoConversion;

import java.math.BigDecimal;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

@Service
public class CryptoConversionService {
	
	@Autowired
	private CryptoExchangeProxy cryptoExchangeProxy;
	
	@Autowired
	private CryptoWalletProxy cryptoWalletProxy;
	
	public ResponseEntity<MessageDto<String, CryptoWalletDto>> getConversion(String from, String to, BigDecimal quantity, String auth) {
		CryptoConversion exchangeRate = cryptoExchangeProxy.getExchange(from, to);
		
		if (exchangeRate == null) {
			MessageDto<String, CryptoWalletDto> messageDto = new MessageDto<>("There was a problem with the crypto exchange service", null);
			return ResponseEntity.status(HttpStatus.CONFLICT).body(messageDto);
		}
		
		CryptoWalletDto userAccount = cryptoWalletProxy.getUserAccount(auth);
		
		if (!checkBalance(userAccount, from.toUpperCase(), quantity)) { 
			MessageDto<String, CryptoWalletDto> messageDto = new MessageDto<>("The balance on the account is not enough", null);
			return ResponseEntity.status(HttpStatus.NOT_ACCEPTABLE).body(messageDto);
		}
		
		userAccount = updateBalance(userAccount, exchangeRate, quantity);
		
		ResponseEntity<?> response = cryptoWalletProxy.updateUserAccount(userAccount);
		
		if(response.getStatusCode() == HttpStatus.OK) {
			MessageDto<String, CryptoWalletDto> messageDto = new MessageDto<>("Conversion and transfer successful for the amount of " + quantity + " " + from + " to " 
						+ quantity.multiply(exchangeRate.getConversionMultiple()) + " " + to, userAccount);
			return ResponseEntity.status(HttpStatus.OK).body(messageDto);
		} else {
			MessageDto<String, CryptoWalletDto> messageDto = new MessageDto<>("Failed to update user's crypto wallet", null);
			return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(messageDto);
		}
		
	}
	
	private boolean isBalanceGreater(BigDecimal balance, BigDecimal quantity) {
		return balance.compareTo(quantity) >= 0;
	}
	
	private boolean checkBalance(CryptoWalletDto userAccount, String from, BigDecimal quantity) {
		switch (from) {
			case "BTC":
				return isBalanceGreater(userAccount.getBtc(), quantity); 
			case "ETH":
				return isBalanceGreater(userAccount.getEth(), quantity); 
			case "BNB":
				return isBalanceGreater(userAccount.getBnb(), quantity);
			case "ADA":
				return isBalanceGreater(userAccount.getAda(), quantity); 
			default:
				return false;
		}
	}
	
	private CryptoWalletDto updateBalance(CryptoWalletDto userAccount, CryptoConversion exchangeRate, BigDecimal quantity) {
		BigDecimal amount = quantity.multiply(exchangeRate.getConversionMultiple());;
		
		switch (exchangeRate.getFrom()) {
			case "BTC":
				
				switch (exchangeRate.getTo()) {
					case "ETH":
						userAccount.setBtc(userAccount.getBtc().subtract(quantity));
						userAccount.setEth(userAccount.getEth().add(amount));
						
						break;
					case "BNB":
						userAccount.setBtc(userAccount.getBtc().subtract(quantity));
						userAccount.setBnb(userAccount.getBnb().add(amount));
						
						break;
					case "ADA":
						userAccount.setBtc(userAccount.getBtc().subtract(quantity));
						userAccount.setAda(userAccount.getAda().add(amount));
						
						break;
				}
				
				break;
			case "ETH":
				
				switch (exchangeRate.getTo()) {
					case "BTC":
						userAccount.setEth(userAccount.getEth().subtract(quantity));
						userAccount.setBtc(userAccount.getBtc().add(amount));
						
						break;
					case "BNB":
						userAccount.setEth(userAccount.getEth().subtract(quantity));
						userAccount.setBnb(userAccount.getBnb().add(amount));
						
						break;
					case "ADA":
						userAccount.setEth(userAccount.getEth().subtract(quantity));
						userAccount.setAda(userAccount.getAda().add(amount));
						
						break;
				}
				
				break;
			case "BNB":
				
				switch (exchangeRate.getTo()) {
					case "BTC":
						userAccount.setBnb(userAccount.getBnb().subtract(quantity));
						userAccount.setBtc(userAccount.getBtc().add(amount));
						
						break;
					case "ETH":
						userAccount.setBnb(userAccount.getBtc().subtract(quantity));
						userAccount.setEth(userAccount.getEth().add(amount));
						
						break;
					case "ADA":
						userAccount.setBnb(userAccount.getBtc().subtract(quantity));
						userAccount.setAda(userAccount.getAda().add(amount));
						
						break;
				}
				
				break;
			case "ADA":
				switch (exchangeRate.getTo()) {
					case "BTC":
						userAccount.setAda(userAccount.getAda().subtract(quantity));
						userAccount.setBtc(userAccount.getBtc().add(amount));
						
						break;
					case "ETH":
						userAccount.setAda(userAccount.getAda().subtract(quantity));
						userAccount.setEth(userAccount.getEth().add(amount));
						
						break;
					case "BNB":
						userAccount.setAda(userAccount.getAda().subtract(quantity));
						userAccount.setBnb(userAccount.getBnb().add(amount));
						
						break;
				}
				
				break;
		}
		
		return userAccount;
	}

}
