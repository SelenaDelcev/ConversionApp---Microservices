package cryptoConversion;

import java.math.BigDecimal;

public class CryptoWalletDto {

	private String email;
	private BigDecimal btc;
	private BigDecimal eth;
	private BigDecimal bnb;
	private BigDecimal ada;

	public CryptoWalletDto() {

	}

	public CryptoWalletDto(String email, BigDecimal btc, BigDecimal eth, BigDecimal bnb, BigDecimal ada) {
		this.email = email;
		this.btc = btc;
		this.eth = eth;
		this.bnb = bnb;
		this.ada = ada;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public BigDecimal getBtc() {
		return btc;
	}

	public void setBtc(BigDecimal btc) {
		this.btc = btc;
	}

	public BigDecimal getEth() {
		return eth;
	}

	public void setEth(BigDecimal eth) {
		this.eth = eth;
	}

	public BigDecimal getBnb() {
		return bnb;
	}

	public void setBnb(BigDecimal bnb) {
		this.bnb = bnb;
	}

	public BigDecimal getAda() {
		return ada;
	}

	public void setAda(BigDecimal ada) {
		this.ada = ada;
	}

}
