package currencyConversion;

public class MessageDto<S, T> {
	
	private S message;
	private T bankAccountData;
	
	public MessageDto(S message, T data) {
		this.message = message;
		this.bankAccountData = data;
	}
	 
	public S getMessage() {
		return message;
	}
	
	public T getData() {
		return bankAccountData;
	}
}
