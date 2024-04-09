package cryptoWallet;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(name = "bank-account")
public interface BankAccountProxy {

    @GetMapping("bank-account/check")
    public boolean checkBankAccount(@RequestParam("email") String email);
}
