package usersService;

public class UpdateDto {
	private String email;
    private String password;
    
    public UpdateDto() {
    	
    }
    
    public UpdateDto(String email, String password) {
		this.email = email;
		this.password = password;
    }   

	public String getEmail() {
		return email;
	}
	
	public void setEmail(String email) {
		this.email = email;
	}
	
	public String getPassword() {
		return password;
	}
	
	public void setPassword(String password) {
		this.password = password;
	}

}
