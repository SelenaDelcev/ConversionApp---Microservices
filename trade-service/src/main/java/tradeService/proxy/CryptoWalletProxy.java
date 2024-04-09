package tradeService.proxy;

import java.math.BigDecimal;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestHeader;

import tradeService.dto.CryptoWalletDto;

@FeignClient("crypto-wallet")
public interface CryptoWalletProxy {
	
	@GetMapping("/crypto-wallet")
	public CryptoWalletDto getUserAccount(@RequestHeader("Authorization") String auth);
	
	@PutMapping("/crypto-wallet/{email}/update/{update}/quantity/{quantity}")
	CryptoWalletDto updateWalletAfterTrade(@PathVariable String email, @PathVariable String update, @PathVariable BigDecimal quantity);

}
