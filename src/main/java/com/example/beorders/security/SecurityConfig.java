package com.example.beorders.security;

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

import com.example.beorders.Role;

@Configuration
class SecurityConfig {

	@Bean
	SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
		http
		.authorizeHttpRequests(request -> request
				.requestMatchers("/v1/admin/orders/**")
				.hasAnyRole(Role.ADMIN.name()))
				.httpBasic(Customizer.withDefaults())
		.authorizeHttpRequests(request -> request
				.requestMatchers("/v1/orders/**")
				.hasAnyRole(Role.ADMIN.name(), Role.ORDER_OWNER.name()))
				.httpBasic(Customizer.withDefaults())
		.csrf(csrf -> csrf.disable());

		return http.build();
	}

	
	@Bean
	UserDetailsService testOnlyUsers(PasswordEncoder passwordEncoder) {
		User.UserBuilder users = User.builder();

		UserDetails userAdmin = createUser("Admin", "admin", Role.ADMIN.name(), passwordEncoder);
		UserDetails userAlice = createUser("Alice", "alice", Role.ORDER_OWNER.name(), passwordEncoder);
		UserDetails userBoris = createUser("Boris", "boris", Role.NON_ORDER_OWNER.name(), passwordEncoder);
		UserDetails userCathy = createUser("Cathy", "cathy", Role.ORDER_OWNER.name(), passwordEncoder);

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