package cryptoWallet;

import org.springframework.data.jpa.repository.JpaRepository;

public interface CryptoWalletRepository extends JpaRepository<CryptoWallet, Long> {
	
	CryptoWallet findByEmail(String email);
	
}
