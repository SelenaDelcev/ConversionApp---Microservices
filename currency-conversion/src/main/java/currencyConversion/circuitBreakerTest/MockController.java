package currencyConversion.circuitBreakerTest;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

@RestController
public class MockController {
	
	@GetMapping("/sendRequeests")
	public String sendRequests() {
		String response = "";
		for(int i=1; i<=10000; i++) {
			response = new RestTemplate()
					.getForEntity("http://localhost:8000/sample/api", String.class).getBody();
		}
		
		return response;
	}

}
