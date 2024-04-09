package cryptoExchange;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;

@RestController
public class CryptoExchangeController {
	
	@Autowired
	private CryptoExchangeRepository repo;
	
	@Autowired 
	private Environment environment;
	
	@GetMapping("/crypto-exchange/from/{from}/to/{to}")
	@CircuitBreaker(name = "exchangeCB", fallbackMethod = "fallbackExchange")
	public ResponseEntity<?> getExchange(@PathVariable String from, @PathVariable String to) {
		String port = environment.getProperty("local.server.port");
		CryptoExchange kurs = repo.findByFromAndToIgnoreCase(from, to);
		
		if(kurs!=null) {
			kurs.setEnvironment(port);
			return ResponseEntity.ok(kurs);
		}else {
			return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Requested crypto exchange could not be found!");
		}
		
	}
	
	public ResponseEntity<?> fallbackExchange(String from, String to, Throwable throwable) {
        // Fallback response in case of circuit breaker open state
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                .body("Crypto exchange service is currently unavailable. Please try again later.");
    }

}
