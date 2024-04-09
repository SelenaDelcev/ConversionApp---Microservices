package cryptoWallet;

import java.math.BigDecimal;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;

@Entity
public class CryptoWallet {
	
	@Id
	private Long id;

	@Column(unique = true, nullable = false)
	private String email;

	// Currency amounts defined for currency exchange microservice
	@Column
	private BigDecimal btc;

	@Column
	private BigDecimal eth;

	@Column
	private BigDecimal bnb;

	@Column
	private BigDecimal ada;
	
	public CryptoWallet() {
		
	}

	public CryptoWallet(Long id, String email, BigDecimal btc, BigDecimal eth, BigDecimal bnb, BigDecimal ada) {
		this.id = id;
		this.email = email;
		this.btc = btc;
		this.eth = eth;
		this.bnb = bnb;
		this.ada = ada;
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
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
