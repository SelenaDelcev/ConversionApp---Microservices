package cryptoWallet;

import java.math.BigDecimal;

public class CryptoWalletUpdateDto {
	
	private BigDecimal btc;
	private BigDecimal eth;
	private BigDecimal bnb;
	private BigDecimal ada;
	
	public CryptoWalletUpdateDto() {
		
	}

	public CryptoWalletUpdateDto(BigDecimal btc, BigDecimal eth, BigDecimal bnb, BigDecimal ada) {
		this.btc = btc;
		this.eth = eth;
		this.bnb = bnb;
		this.ada = ada;
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
