package com.example.beorders;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
class SecurityConfig {

	@Bean
	SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
		http
		.authorizeHttpRequests(request -> request
				.requestMatchers("/admin/orders/**")
				.hasAnyRole("ADMIN"))
				.httpBasic(Customizer.withDefaults())
		.authorizeHttpRequests(request -> request
				.requestMatchers("/orders/**")
				.hasAnyRole("ADMIN", "ORDER_OWNER"))
				.httpBasic(Customizer.withDefaults())
		.csrf(csrf -> csrf.disable());

		return http.build();
	}

	
	@Bean
	UserDetailsService testOnlyUsers(PasswordEncoder passwordEncoder) {
		User.UserBuilder users = User.builder();

		UserDetails userAdmin = createUser("Admin", "admin", "ADMIN", passwordEncoder);
		UserDetails userAlice = createUser("Alice", "alice", "ORDER_OWNER", passwordEncoder);
		UserDetails userBoris = createUser("Boris", "boris", "NON_ORDER_OWNER", passwordEncoder);
		UserDetails userCathy = createUser("Cathy", "cathy", "ORDER_OWNER", passwordEncoder);

		return new InMemoryUserDetailsManager(userAdmin, userAlice, userBoris, userCathy);
	}
	
	
	@Bean
	PasswordEncoder passwordEncoder() {
		return new BCryptPasswordEncoder();
	}
	
	
	private UserDetails createUser(String username, String password, String role, PasswordEncoder passwordEncoder) {
		return User
			.builder()
			.username(username)
			.password(passwordEncoder.encode(password))
			.roles(role)
			.build();
	}
}