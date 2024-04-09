package tradeService.dto;

public class MessageDto<S, T> {
	
	private S message;
	private T data;
	
	public MessageDto(S message, T data) {
		this.message = message;
		this.data = data;
	}
	 
	public S getMessage() {
		return message;
	}
	
	public T getData() {
		return data;
	}
}
