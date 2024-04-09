package tradeService;

import java.math.BigDecimal;
import java.math.RoundingMode;

import org.apache.hc.client5.http.utils.Base64;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RequestParam;

import tradeService.dto.BankAccountDto;
import tradeService.dto.CryptoWalletDto;
import tradeService.dto.CurrencyExchangeDto;
import tradeService.dto.MessageDto;
import tradeService.proxy.BankAccountProxy;
import tradeService.proxy.CryptoWalletProxy;
import tradeService.proxy.CurrencyExchangeProxy;

@Service
public class TradeMicroService {

	@Autowired
	TradeServiceRepository repo;

	@Autowired
	private CryptoWalletProxy walletProxy;

	@Autowired
	private CurrencyExchangeProxy currencyExchangeProxy;

	@Autowired
	private BankAccountProxy bankProxy;

	double addTo;
	double quantityToReduce;

	public  ResponseEntity<MessageDto<String, ?>> trade(@RequestParam String from, @RequestParam String to,
			@RequestParam(defaultValue = "10") BigDecimal quantity, String auth) {

		String pair = new String(Base64.decodeBase64(auth.substring(6)));
		String email = pair.split(":")[0];

		if (!isValidCurrency(from.toUpperCase()) && !isValidCrypto(from.toUpperCase())) {
			MessageDto<String, Object> messageDto = new MessageDto<>("Not valid currency in FROM parameter", null);
			return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(messageDto);
		}

		if (!isValidCurrency(to.toUpperCase()) && !isValidCrypto(to.toUpperCase())) {
			MessageDto<String, Object> messageDto = new MessageDto<>("Not valid currency in TO parameter", null);
			return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(messageDto);
		}

		if ((from.toUpperCase().equals("RSD") || from.toUpperCase().equals("EUR") || from.toUpperCase().equals("USD")
				|| from.toUpperCase().equals("GBP") || from.toUpperCase().equals("CHF"))
				&& (to.toUpperCase().equals("RSD") || to.toUpperCase().equals("EUR") || to.toUpperCase().equals("USD")
						|| to.toUpperCase().equals("GBP") || to.toUpperCase().equals("CHF"))) {
			MessageDto<String, Object> messageDto = new MessageDto<>(
					"This is crypto trade, one currency must be either crypto or fiat currency", null);
			return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(messageDto);
		}

		if ((from.toUpperCase().equals("BTC") || from.toUpperCase().equals("ETH") || from.toUpperCase().equals("BNB")
				|| from.toUpperCase().equals("ADA"))
				&& (to.toUpperCase().equals("BTC") || to.toUpperCase().equals("ETH") || to.toUpperCase().equals("BNB")
						|| to.toUpperCase().equals("ADA"))) {
			MessageDto<String, Object> messageDto = new MessageDto<>(
					"This is crypto trade, one currency must be either crypto or fiat currency", null);
			return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(messageDto);
		}

		if (from.toUpperCase().equals("BTC") || from.toUpperCase().equals("ETH") || from.toUpperCase().equals("BNB")
				|| from.toUpperCase().equals("ADA")) {

			CryptoWalletDto wallet = walletProxy.getUserAccount(auth);
			if (wallet == null) {
				MessageDto<String, CryptoWalletDto> messageDto = new MessageDto<>("Crypto wallet not found", null);
				return ResponseEntity.status(HttpStatus.NOT_FOUND).body(messageDto);
			}

			if (from.toUpperCase().equals("BTC")) {
				if (wallet.getBtc().compareTo(quantity) < 0) {
					MessageDto<String, CryptoWalletDto> messageDto = new MessageDto<>("Not enough BTC on account", wallet);
					return ResponseEntity.status(HttpStatus.NOT_ACCEPTABLE).body(messageDto);
				}
			} else if (from.toUpperCase().equals("ETH")) {
				if (wallet.getEth().compareTo(quantity) < 0) {
					MessageDto<String, CryptoWalletDto> messageDto = new MessageDto<>("Not enough ETH on account", wallet);
					return ResponseEntity.status(HttpStatus.NOT_ACCEPTABLE).body(messageDto);
				}
			} else if (from.toUpperCase().equals("BNB")) {
				if (wallet.getAda().compareTo(quantity) < 0) {
					MessageDto<String, CryptoWalletDto> messageDto = new MessageDto<>("Not enough BNB on account", wallet);
					return ResponseEntity.status(HttpStatus.NOT_ACCEPTABLE).body(messageDto);
				}
			} else if (from.toUpperCase().equals("ADA")) {
				if (wallet.getAda().compareTo(quantity) < 0) {
					MessageDto<String, CryptoWalletDto> messageDto = new MessageDto<>("Not enough ADA on account", wallet);
					return ResponseEntity.status(HttpStatus.NOT_ACCEPTABLE).body(messageDto);
				}
			}

			quantityToReduce = 0 - quantity.doubleValue();

			if (to.toUpperCase().equals("EUR") || to.toUpperCase().equals("USD")) {
				TradeService tradeService = repo.findByFromAndTo(from.toUpperCase(), to.toUpperCase());

				addTo = quantity.multiply(tradeService.getConversionMultiple()).doubleValue();
			} else if (to.toUpperCase().equals("CHF") || to.toUpperCase().equals("GBP")
					|| to.toUpperCase().equals("RSD")) {

				TradeService tradeService = repo.findByFromAndTo(from.toUpperCase(), "USD");

				CurrencyExchangeDto ce = currencyExchangeProxy.getExchange("USD", to.toUpperCase());

				addTo = quantity.multiply(tradeService.getConversionMultiple()).multiply(ce.getConversionMultiple())
						.doubleValue();
			}

			walletProxy.updateWalletAfterTrade(email, from, new BigDecimal(addTo).setScale(5, RoundingMode.HALF_UP));

			BankAccountDto bankAccount = bankProxy.updateAccountAfterTrade(email, to,
					new BigDecimal(addTo).setScale(5, RoundingMode.HALF_UP));

			MessageDto<String, BankAccountDto> messageDto = new MessageDto<>("Conversion and transfer successful for the amount of "
					+ quantity + " " + from + " to " + addTo + " " + to, bankAccount);
			return ResponseEntity.status(HttpStatus.OK).body(messageDto);
		} else if (from.toUpperCase().equals("EUR") || from.toUpperCase().equals("USD")) {
			BankAccountDto bankAccount = bankProxy.getUserAccount(auth);
			if (bankAccount == null) {
				MessageDto<String, BankAccountDto> messageDto = new MessageDto<>("Bank account is not found", null);
				return ResponseEntity.status(HttpStatus.NOT_FOUND).body(messageDto);
			}

			if (from.toUpperCase().equals("EUR")) {
				if (bankAccount.getEur().compareTo(quantity) < 0) {
					MessageDto<String, BankAccountDto> messageDto = new MessageDto<>("Not enough EUR on account", bankAccount);
					return ResponseEntity.status(HttpStatus.NOT_ACCEPTABLE).body(messageDto);
				}
			} else if (from.toUpperCase().equals("USD")) {
				if (bankAccount.getUsd().compareTo(quantity) < 0) {
					MessageDto<String, BankAccountDto> messageDto = new MessageDto<>("Not enough USD on account", bankAccount);
					return ResponseEntity.status(HttpStatus.NOT_ACCEPTABLE).body(messageDto);
				}
			}

			quantityToReduce = 0 - quantity.doubleValue();

			TradeService tradeService = repo.findByFromAndTo(from.toUpperCase(), to.toUpperCase());

			addTo = quantity.multiply(tradeService.getConversionMultiple()).doubleValue();

			CryptoWalletDto wallet = walletProxy.updateWalletAfterTrade(email, to,
					new BigDecimal(addTo).setScale(5, RoundingMode.HALF_UP));

			bankProxy.updateAccountAfterTrade(email, from, new BigDecimal(quantityToReduce));

			MessageDto<String, CryptoWalletDto> messageDto = new MessageDto<>(
					"Conversion and transfer successful for the amount of " + quantity + " " + from + " to "
							+ quantity.multiply(tradeService.getConversionMultiple()) + " " + to,
					wallet);
			return ResponseEntity.status(HttpStatus.OK).body(messageDto);
		} else if (from.toUpperCase().equals("CHF") || from.toUpperCase().equals("GBP")
				|| from.toUpperCase().equals("RSD")) {
			BankAccountDto bankAccount = bankProxy.getUserAccount(auth);
			if (bankAccount == null) {
				MessageDto<String, BankAccountDto> messageDto = new MessageDto<>("Bank account not found", null);
				return ResponseEntity.status(HttpStatus.NOT_FOUND).body(messageDto);
			}

			if (from.toUpperCase().equals("RSD")) {
				if (bankAccount.getRsd().compareTo(quantity) <= 0) {
					MessageDto<String, BankAccountDto> messageDto = new MessageDto<>("Not enough RSD on account", bankAccount);
					return ResponseEntity.status(HttpStatus.NOT_ACCEPTABLE).body(messageDto);
				}
			} else if (from.toUpperCase().equals("CHF")) {
				if (bankAccount.getChf().compareTo(quantity) <= 0) {
					MessageDto<String, BankAccountDto> messageDto = new MessageDto<>("Not enough CHF on account", bankAccount);
					return ResponseEntity.status(HttpStatus.NOT_ACCEPTABLE).body(messageDto);
				}
			} else if (from.toUpperCase().equals("GBP")) {
				if (bankAccount.getGbp().compareTo(quantity) <= 0) {
					MessageDto<String, BankAccountDto> messageDto = new MessageDto<>("Not enough GBP on account", bankAccount);
					return ResponseEntity.status(HttpStatus.NOT_ACCEPTABLE).body(messageDto);
				}
			}

			quantityToReduce = 0 - quantity.doubleValue();

			TradeService tradeService = repo.findByFromAndTo("USD", to.toUpperCase());

			CurrencyExchangeDto ce = currencyExchangeProxy.getExchange(from.toUpperCase(), "USD");

			addTo = quantity.multiply(tradeService.getConversionMultiple()).multiply(ce.getConversionMultiple())
					.doubleValue();

			CryptoWalletDto wallet = walletProxy.updateWalletAfterTrade(email, to, new BigDecimal(addTo).setScale(5, RoundingMode.HALF_UP));

			bankProxy.updateAccountAfterTrade(email, from, new BigDecimal(quantityToReduce));

			MessageDto<String, CryptoWalletDto> messageDto = new MessageDto<>(
					"Conversion and transfer successful for the amount of " + quantity + " " + from + " to "
							+ quantity.multiply(tradeService.getConversionMultiple()) + " " + to,
					wallet);
			return ResponseEntity.status(HttpStatus.OK).body(messageDto);
		}

		return null;
	}

	private boolean isValidCurrency(String currency) {
		return currency.equals("RSD") || currency.equals("EUR") || currency.equals("USD") || currency.equals("GBP")
				|| currency.equals("CHF");
	}

	private boolean isValidCrypto(String cryptoCurrency) {
		return cryptoCurrency.equals("BTC") || cryptoCurrency.equals("ETH") || cryptoCurrency.equals("BNB")
				|| cryptoCurrency.equals("ADA");
	}
}
