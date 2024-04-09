package tradeService.proxy;

import java.math.BigDecimal;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestHeader;

import tradeService.dto.BankAccountDto;

@FeignClient("bank-account")
public interface BankAccountProxy {
	
	@GetMapping("/bank-account")
	public BankAccountDto getUserAccount(@RequestHeader("Authorization") String auth);
	
	@PutMapping("/bank-account/{email}/update/{update}/quantity/{quantity}")
	BankAccountDto updateAccountAfterTrade(@PathVariable String email, @PathVariable String update, @PathVariable BigDecimal quantity);

}
