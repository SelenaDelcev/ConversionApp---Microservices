package cryptoConversion;

import java.math.BigDecimal;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import feign.FeignException;
import io.github.resilience4j.ratelimiter.RequestNotPermitted;
import io.github.resilience4j.ratelimiter.annotation.RateLimiter;

@RestController
@RequestMapping("/crypto-conversion")
public class CryptoConversionController {

	@Autowired
	private CryptoConversionService service;

	@RateLimiter(name = "defaultRL", fallbackMethod = "rateLimiterResponse")
	@GetMapping
	public ResponseEntity<MessageDto<String, CryptoWalletDto>> getConversionFeign(@RequestParam String from, @RequestParam String to,
			@RequestParam(defaultValue = "10") BigDecimal quantity, @RequestHeader("Authorization") String auth) {

		try {

			return service.getConversion(from, to, quantity, auth);

		} catch (FeignException e) {
			MessageDto<String, CryptoWalletDto> messageDto = new MessageDto<>(e.getMessage(), null);
			return ResponseEntity.status(HttpStatus.CONFLICT).body(messageDto);
		}
	}

	// exception handling ako nedostaje neki RequestParam (poseban nacin jer se
	// exception ne desava u try-catch bloku)
	@ExceptionHandler(MissingServletRequestParameterException.class)
	public ResponseEntity<String> handleMissingParams(MissingServletRequestParameterException ex) {
		return ResponseEntity.status(ex.getStatusCode()).body(
				"Value [" + ex.getParameterType() + "] of parameter [" + ex.getParameterName() + "] has been ommited");
	}

	@ExceptionHandler(RequestNotPermitted.class)
	public ResponseEntity<String> rateLimiterResponse(Exception ex) {
		return ResponseEntity.status(503).body("Currency exchange can serve only up to 2 requests in 45 seconds");
	}

}
