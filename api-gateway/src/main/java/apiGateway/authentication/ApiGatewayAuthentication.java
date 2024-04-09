package apiGateway.authentication;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.ResponseEntity;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.core.userdetails.MapReactiveUserDetailsService;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.server.SecurityWebFilterChain;
import org.springframework.web.client.RestTemplate;

import authentication.dtos.CustomUserDto;

@Configuration
@EnableWebFluxSecurity
public class ApiGatewayAuthentication {
	
	@Bean
	public MapReactiveUserDetailsService userDetailsService(BCryptPasswordEncoder encoder) {
		List<UserDetails> users = new ArrayList<>();
		List<CustomUserDto> usersFromDatabase;
		
		ResponseEntity<CustomUserDto[]> response = 
		//new RestTemplate().getForEntity("http://host.docker.internal:8770/users", CustomUserDto[].class);
		new RestTemplate().getForEntity("http://localhost:8770/users-service/users", CustomUserDto[].class);
		
		usersFromDatabase = Arrays.asList(response.getBody());
		
		for(CustomUserDto cud: usersFromDatabase) {
			users.add(User
					.withUsername(cud.getEmail())
					.password(encoder.encode(cud.getPassword()))
					.roles(cud.getRole())
					.build());
		}
		
		return new MapReactiveUserDetailsService(users);
	}
	
	@Bean
	public BCryptPasswordEncoder getEncoder() {
		return new BCryptPasswordEncoder();
	}
	
	@Bean
	public SecurityWebFilterChain filterChain(ServerHttpSecurity http) throws Exception {
		
		http.csrf().disable().authorizeExchange()
		.pathMatchers("/currency-exchange/**").permitAll()
		.pathMatchers("/currency-conversion").hasRole("USER")
		.pathMatchers("/users-service/users").permitAll()
		.pathMatchers("/users-service/users/removeUser/{email}").hasRole("OWNER")
		.pathMatchers("/users-service/users/addUser").hasRole("ADMIN")
		.pathMatchers("/users-service/users/addUserOrAdmin").hasRole("OWNER")
		.pathMatchers("/users-service/users/updateUser/{email}").hasRole("ADMIN")
		.pathMatchers("/users-service/users/updateAnyone/{email}").hasRole("OWNER")
		.pathMatchers("/bank-account").hasRole("USER")
		.pathMatchers("/bank-account/addUserAccount").hasRole("ADMIN")
		.pathMatchers("/bank-account/editAccount/{email}").hasRole("ADMIN")
		.pathMatchers("/crypto-wallet").hasRole("USER")
		.pathMatchers("/crypto-wallet/addCryptoWallet").hasRole("ADMIN")
		.pathMatchers("/crypto-wallet/editWallet/{email}").hasRole("ADMIN")
		.pathMatchers("/crypto-exchange/**").permitAll()
		.pathMatchers("/crypto-conversion").hasRole("USER")
		.pathMatchers("/trade-service/**").hasRole("USER")
		.and().httpBasic();
		
		return http.build();
	}
}
