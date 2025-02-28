package com.example.beorders;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.net.URI;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;

import com.example.beorders.orders.Order;
import com.jayway.jsonpath.DocumentContext;
import com.jayway.jsonpath.JsonPath;

import net.minidev.json.JSONArray;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class BeordersApplicationAdminTests {
	
	@Autowired
	TestRestTemplate restTemplate;

	@Test
	void contextLoads() {
	}

	
	@Test
	void shouldReturnAnOrderWhenDataIsSavedUsingReservedUri() {
		ResponseEntity<String> response = restTemplate
				.withBasicAuth("Admin", "admin")
				.getForEntity("/admin/orders/99", String.class);

		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
		DocumentContext documentContext = JsonPath.parse(response.getBody());
		
		Number id = documentContext.read("$.id");
		Double amount = documentContext.read("$.amount");
		String username = documentContext.read("$.owner");
		
		assertThat(id).isEqualTo(99);
		assertThat(amount).isEqualTo(123.99);
		assertThat(username).isEqualTo("Alice");
	}
	
	
	@Test
	void shouldReturnAnOrderWhenDataIsSavedUsingPublicUri() {
		ResponseEntity<String> response = restTemplate
				.withBasicAuth("Admin", "admin")
				.getForEntity("/orders/99", String.class);

		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
		DocumentContext documentContext = JsonPath.parse(response.getBody());
		
		Number id = documentContext.read("$.id");
		Double amount = documentContext.read("$.amount");
		String username = documentContext.read("$.owner");
		
		assertThat(id).isEqualTo(99);
		assertThat(amount).isEqualTo(123.99);
		assertThat(username).isEqualTo("Alice");
	}
	
	
	@Test
	void shouldNotReturnAnOrderWithAnUnknownId() {
		ResponseEntity<String> response = restTemplate
				.withBasicAuth("Admin", "admin")
				.getForEntity("/admin/orders/5000", String.class);

		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
		assertThat(response.getBody()).isBlank();
	}
	
	
	@Test
	@DirtiesContext
	void shouldCreateANewOrderForAdminHimself() {
		// owner parameter is null because the owner is taken from the principal
		Order newOrd = new Order(null, 250.00, null);
		ResponseEntity<Void> createResponse = restTemplate
				.withBasicAuth("Admin", "admin")
				.postForEntity("/admin/orders", newOrd, Void.class);
		assertThat(createResponse.getStatusCode()).isEqualTo(HttpStatus.CREATED);
		
		URI locationOfNewOrder = createResponse.getHeaders().getLocation();
		ResponseEntity<String> getResponse = restTemplate
				.withBasicAuth("Admin", "admin")

				.getForEntity(locationOfNewOrder, String.class);
		assertThat(getResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
		
		DocumentContext documentContext = JsonPath.parse(getResponse.getBody());
		Number id = documentContext.read("$.id");
		Double amount = documentContext.read("$.amount");
		String username = documentContext.read("$.owner");

		assertThat(id).isNotNull();
		assertThat(amount).isEqualTo(250.00);
		assertThat(username).isEqualTo("Admin");
	}
	
	
	@Test
	@DirtiesContext
	void shouldCreateANewOrderOnBehalfOthersUsingReservedUri() {
		// admin creates an order on behalf of Alice
		Order newOrd = new Order(null, 250.00, "Alice");
		ResponseEntity<Void> createResponse = restTemplate
				.withBasicAuth("Admin", "admin")
				.postForEntity("/admin/orders", newOrd, Void.class);
		assertThat(createResponse.getStatusCode()).isEqualTo(HttpStatus.CREATED);
		
		// check that update has really changed the data store
		// verification 1: admin should see the new order
		URI locationOfNewOrder = createResponse.getHeaders().getLocation();
		ResponseEntity<String> getResponse = restTemplate
				.withBasicAuth("Admin", "admin")
				.getForEntity(locationOfNewOrder, String.class);
		assertThat(getResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
		
		DocumentContext documentContext = JsonPath.parse(getResponse.getBody());
		Number id = documentContext.read("$.id");
		Double amount = documentContext.read("$.amount");
		String owner = documentContext.read("$.owner");

		assertThat(id).isNotNull();
		assertThat(amount).isEqualTo(250.00);
		assertThat(owner).isEqualTo("Alice");
		
		// verification 2: but also Alice (the owner) should see it under the URI "/orders/{id}"
		// example:
		// - URI from location header: http://localhost:50449/admin/orders/1
		// - URI for Alice: http://localhost:50449/orders/1
		String uriFromLocationHeader = createResponse.getHeaders().getLocation().toASCIIString();
		String newUrlForOwner = uriFromLocationHeader.replaceAll("/admin/", "/");
		
		ResponseEntity<String> getOwnerResponse = restTemplate
				.withBasicAuth("Alice", "alice")
				.getForEntity(newUrlForOwner, String.class);
		assertThat(getOwnerResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
		
		DocumentContext documentContextFromOwnerResponse = JsonPath.parse(getOwnerResponse.getBody());
		Number idFromOwnerResponse = documentContextFromOwnerResponse.read("$.id");
		Double amountFromOwnerResponse = documentContextFromOwnerResponse.read("$.amount");
		String ownerFromOwnerResponse = documentContextFromOwnerResponse.read("$.owner");

		assertThat(idFromOwnerResponse).isNotNull();
		assertThat(amountFromOwnerResponse).isEqualTo(250.00);
		assertThat(ownerFromOwnerResponse).isEqualTo("Alice");
	}
	
	
	@Test
	@DirtiesContext
	void shouldCreateANewOrderOnBehalfOthersUsingPublicUri() {
		// admin creates an order on behalf of Alice
		Order newOrd = new Order(null, 250.00, "Alice");
		ResponseEntity<Void> createResponse = restTemplate
				.withBasicAuth("Admin", "admin")
				.postForEntity("/orders", newOrd, Void.class);
		assertThat(createResponse.getStatusCode()).isEqualTo(HttpStatus.CREATED);
		
		// check that update has really changed the data store
		// verification 1: admin should see the new order
		URI locationOfNewOrder = createResponse.getHeaders().getLocation();
		ResponseEntity<String> getResponse = restTemplate
				.withBasicAuth("Admin", "admin")
				.getForEntity(locationOfNewOrder, String.class);
		assertThat(getResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
		
		DocumentContext documentContext = JsonPath.parse(getResponse.getBody());
		Number id = documentContext.read("$.id");
		Double amount = documentContext.read("$.amount");
		String owner = documentContext.read("$.owner");

		assertThat(id).isNotNull();
		assertThat(amount).isEqualTo(250.00);
		assertThat(owner).isEqualTo("Alice");
		
		// verification 2: but also Alice (the owner) should see it under the URI "/orders/{id}"
		// example:
		// - URI from location header: http://localhost:50449/admin/orders/1
		// - URI for Alice: http://localhost:50449/orders/1
		String uriFromLocationHeader = createResponse.getHeaders().getLocation().toASCIIString();
		String newUrlForOwner = uriFromLocationHeader.replaceAll("/admin/", "/");
		
		ResponseEntity<String> getOwnerResponse = restTemplate
				.withBasicAuth("Alice", "alice")
				.getForEntity(newUrlForOwner, String.class);
		assertThat(getOwnerResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
		
		DocumentContext documentContextFromOwnerResponse = JsonPath.parse(getOwnerResponse.getBody());
		Number idFromOwnerResponse = documentContextFromOwnerResponse.read("$.id");
		Double amountFromOwnerResponse = documentContextFromOwnerResponse.read("$.amount");
		String ownerFromOwnerResponse = documentContextFromOwnerResponse.read("$.owner");

		assertThat(idFromOwnerResponse).isNotNull();
		assertThat(amountFromOwnerResponse).isEqualTo(250.00);
		assertThat(ownerFromOwnerResponse).isEqualTo("Alice");
	}

	
	@Test
	void shouldReturnAllOrdersOfEveryoneWhenListIsRequestedUsingReservedUri() {
		ResponseEntity<String> response = restTemplate
				.withBasicAuth("Admin", "admin")
				.getForEntity("/admin/orders", String.class);
		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
		
		DocumentContext documentContext = JsonPath.parse(response.getBody());
		int ordersCount = documentContext.read("$.length()");
		assertThat(ordersCount).isEqualTo(7);
		
		JSONArray ids = documentContext.read("$..id");
		assertThat(ids).containsExactlyInAnyOrder(99, 100, 200, 300, 400, 600, 1000);
		
		JSONArray amounts = documentContext.read("$..amount");
		assertThat(amounts).containsExactlyInAnyOrder(123.99, 1100.99, 1200.99, 1300.99, 1400.99, 1600.99, 1968.05);
		
		JSONArray owners = documentContext.read("$..owner");
		assertThat(owners).containsExactlyInAnyOrder("Alice", "Alice", "Alice", "Alice", "Alice", "Cathy", "Admin");
	}

	
	@Test
	void shouldReturnAllOrdersOfEveryoneWhenListIsRequestedUsingPublicUri() {
		ResponseEntity<String> response = restTemplate
				.withBasicAuth("Admin", "admin")
				.getForEntity("/orders", String.class);
		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
		
		DocumentContext documentContext = JsonPath.parse(response.getBody());
		int ordersCount = documentContext.read("$.length()");
		assertThat(ordersCount).isEqualTo(7);
		
		JSONArray ids = documentContext.read("$..id");
		assertThat(ids).containsExactlyInAnyOrder(99, 100, 200, 300, 400, 600, 1000);
		
		JSONArray amounts = documentContext.read("$..amount");
		assertThat(amounts).containsExactlyInAnyOrder(123.99, 1100.99, 1200.99, 1300.99, 1400.99, 1600.99, 1968.05);
		
		JSONArray owners = documentContext.read("$..owner");
		assertThat(owners).containsExactlyInAnyOrder("Alice", "Alice", "Alice", "Alice", "Alice", "Cathy", "Admin");
	}
	
	
	@Test
	@DirtiesContext
	void shouldUpdateAnExistingOrderOfAdminUsingReservedUri() {
		Order orderUpdate = new Order(null, 200.00, "Admin");
		HttpEntity<Order> request = new HttpEntity<>(orderUpdate);
		ResponseEntity<Void> response = restTemplate
				.withBasicAuth("Admin", "admin")
				.exchange("/admin/orders/1000", HttpMethod.PUT, request, Void.class);
		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
		
		// check that update has really changed the data store
		ResponseEntity<String> responseToGet = restTemplate
				.withBasicAuth("Admin", "admin")
				.getForEntity("/admin/orders/1000", String.class);
		assertThat(responseToGet.getStatusCode()).isEqualTo(HttpStatus.OK);
		DocumentContext documentContext = JsonPath.parse(responseToGet.getBody());
		Number id = documentContext.read("$.id");
		Double amount = documentContext.read("$.amount");
		String owner = documentContext.read("$.owner");
		
		assertThat(id).isEqualTo(1000);
		assertThat(amount).isEqualTo(200.00);
		assertThat(owner).isEqualTo("Admin");
	}
	
	
	@Test
	@DirtiesContext
	void shouldUpdateAnExistingOrderOfAdminUsingPublicUri() {
		Order orderUpdate = new Order(null, 200.00, "Admin");
		HttpEntity<Order> request = new HttpEntity<>(orderUpdate);
		ResponseEntity<Void> response = restTemplate
				.withBasicAuth("Admin", "admin")
				.exchange("/orders/1000", HttpMethod.PUT, request, Void.class);
		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
		
		// check that update has really changed the data store
		ResponseEntity<String> responseToGet = restTemplate
				.withBasicAuth("Admin", "admin")
				.getForEntity("/orders/1000", String.class);
		assertThat(responseToGet.getStatusCode()).isEqualTo(HttpStatus.OK);
		DocumentContext documentContext = JsonPath.parse(responseToGet.getBody());
		Number id = documentContext.read("$.id");
		Double amount = documentContext.read("$.amount");
		String owner = documentContext.read("$.owner");
		
		assertThat(id).isEqualTo(1000);
		assertThat(amount).isEqualTo(200.00);
		assertThat(owner).isEqualTo("Admin");
	}
	
	
	@Test
	@DirtiesContext
	void shouldUpdateAnExistingOrderOnBehalfOfOthersUsingReservedUri() {
		Order orderUpdate = new Order(null, 200.00, "Alice");
		HttpEntity<Order> request = new HttpEntity<>(orderUpdate);
		ResponseEntity<Void> response = restTemplate
				.withBasicAuth("Admin", "admin")
				.exchange("/admin/orders/99", HttpMethod.PUT, request, Void.class);
		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
		
		// check that update has really changed the data store
		// verification 1: admin should see the new order
		ResponseEntity<String> responseToGet = restTemplate
				.withBasicAuth("Admin", "admin")
				.getForEntity("/admin/orders/99", String.class);
		
		assertThat(responseToGet.getStatusCode()).isEqualTo(HttpStatus.OK);
		DocumentContext documentContext = JsonPath.parse(responseToGet.getBody());
		Number id = documentContext.read("$.id");
		Double amount = documentContext.read("$.amount");
		String owner = documentContext.read("$.owner");
		
		assertThat(id).isEqualTo(99);
		assertThat(amount).isEqualTo(200.00);
		assertThat(owner).isEqualTo("Alice");
	}
	
	
	@Test
	@DirtiesContext
	void shouldUpdateAnExistingOrderOnBehalfOfOthersUsingPublicUri() {
		Order orderUpdate = new Order(null, 200.00, "Alice");
		HttpEntity<Order> request = new HttpEntity<>(orderUpdate);
		ResponseEntity<Void> response = restTemplate
				.withBasicAuth("Admin", "admin")
				.exchange("/orders/99", HttpMethod.PUT, request, Void.class);
		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
		
		// check that update has really changed the data store
		// verification 1: admin should see the new order
		ResponseEntity<String> responseToGet = restTemplate
				.withBasicAuth("Admin", "admin")
				.getForEntity("/orders/99", String.class);
		
		assertThat(responseToGet.getStatusCode()).isEqualTo(HttpStatus.OK);
		DocumentContext documentContext = JsonPath.parse(responseToGet.getBody());
		Number id = documentContext.read("$.id");
		Double amount = documentContext.read("$.amount");
		String owner = documentContext.read("$.owner");
		
		assertThat(id).isEqualTo(99);
		assertThat(amount).isEqualTo(200.00);
		assertThat(owner).isEqualTo("Alice");
	}

	
	@Test
	void shouldReturnAPageOfOrdersUsingReservedUri() {
		ResponseEntity<String> response = restTemplate
				.withBasicAuth("Admin", "admin")
				.getForEntity("/admin/orders?page=0&size=1", String.class);
		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);

		DocumentContext documentContext = JsonPath.parse(response.getBody());
		JSONArray page = documentContext.read("$[*]");
		assertThat(page).hasSize(1);
	}
	
	@Test
	void shouldReturnAPageOfOrdersUsingPublicUri() {
		ResponseEntity<String> response = restTemplate
				.withBasicAuth("Admin", "admin")
				.getForEntity("/orders?page=0&size=1", String.class);
		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);

		DocumentContext documentContext = JsonPath.parse(response.getBody());
		JSONArray page = documentContext.read("$[*]");
		assertThat(page).hasSize(1);
	}
	
	
	@Test
	void shouldReturnASortedPageOfOrdersUsingReservedUri() {
		ResponseEntity<String> response = restTemplate
				.withBasicAuth("Admin", "admin")
				.getForEntity("/admin/orders?page=0&size=1&sort=amount,desc", String.class);
		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);

		DocumentContext documentContext = JsonPath.parse(response.getBody());
		JSONArray read = documentContext.read("$[*]");
		assertThat(read).hasSize(1);

		double amount = documentContext.read("$[0].amount");
		String owner = documentContext.read("$[0].owner");
		assertThat(amount).isEqualTo(1968.05);
		assertThat(owner).isEqualTo("Admin");
	}
	
	
	@Test
	void shouldReturnASortedPageOfOrdersUsingPublicUri() {
		ResponseEntity<String> response = restTemplate
				.withBasicAuth("Admin", "admin")
				.getForEntity("/orders?page=0&size=1&sort=amount,desc", String.class);
		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);

		DocumentContext documentContext = JsonPath.parse(response.getBody());
		JSONArray read = documentContext.read("$[*]");
		assertThat(read).hasSize(1);

		double amount = documentContext.read("$[0].amount");
		String owner = documentContext.read("$[0].owner");
		assertThat(amount).isEqualTo(1968.05);
		assertThat(owner).isEqualTo("Admin");
	}
	
	
	@Test
	void shouldReturnASortedPageOfOrdersWithNoParametersAndUseDefaultValues() {
		ResponseEntity<String> response = restTemplate
				.withBasicAuth("Admin", "admin")
				.getForEntity("/admin/orders", String.class);
		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);

		DocumentContext documentContext = JsonPath.parse(response.getBody());
		JSONArray page = documentContext.read("$[*]");
		assertThat(page).hasSize(7);

		JSONArray amounts = documentContext.read("$..amount");
		JSONArray owners = documentContext.read("$..owner");

		assertThat(amounts).containsExactly(123.99, 1100.99, 1200.99, 1300.99, 1400.99, 1600.99, 1968.05);
		assertThat(owners).containsExactlyInAnyOrder("Alice", "Alice", "Alice", "Alice", "Alice", "Cathy", "Admin");
	}
	
	
	@Test
	void shouldNotReturnAnOrderWhenUsingBadUsername() {
	    ResponseEntity<String> response = restTemplate
	      .withBasicAuth("BAD_USERNAME", "alice")
	      .getForEntity("/admin/orders/99", String.class);
	    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
	}
	
	
	@Test
	void shouldNotReturnAnOrderWhenUsingBadPassword() {
	    ResponseEntity<String> response = restTemplate
	      .withBasicAuth("Admin", "BAD_PASSWORD")
	      .getForEntity("/orders/99", String.class);
	    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
	}
	
	
	@Test
	void shouldNotReturnAnOrderWhenUsingBadCredentials() {
	    ResponseEntity<String> response = restTemplate
	      .withBasicAuth("BAD_USER", "BAD_PASSWORD")
	      .getForEntity("/admin/orders/99", String.class);
	    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
	}
	
	
	@Test
	void shouldNotUpdateAnOrderThatDoesNotExistUsingReservedUri() {
		Order unknownOrder = new Order(null, 19.99, null);
		HttpEntity<Order> request = new HttpEntity<>(unknownOrder);
		ResponseEntity<Void> response = restTemplate
				.withBasicAuth("Admin", "admin")
				.exchange("/admin/orders/99999", HttpMethod.PUT, request, Void.class);
	
		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
	}
	
	
	@Test
	void shouldNotUpdateAnOrderThatDoesNotExistUsingPublicUri() {
		Order unknownOrder = new Order(null, 19.99, null);
		HttpEntity<Order> request = new HttpEntity<>(unknownOrder);
		ResponseEntity<Void> response = restTemplate
				.withBasicAuth("Admin", "admin")
				.exchange("/orders/99999", HttpMethod.PUT, request, Void.class);
	
		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
	}

	
	@Test
	@DirtiesContext
	void shouldAdminDeleteAnExistingOrderOfAdminUsingReservedUri() {
		ResponseEntity<Void> response = restTemplate
			.withBasicAuth("Admin", "admin")
			.exchange("/admin/orders/1000", HttpMethod.DELETE, null, Void.class);

		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
		
		// now test that the order is actually deleted
		ResponseEntity<String> getResponse = restTemplate
				.withBasicAuth("Admin", "admin")
			.getForEntity("/admin/orders/1000", String.class);
			assertThat(getResponse.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
	}
	
	
	@Test
	@DirtiesContext
	void shouldAdminDeleteAnExistingOrderOfAdminUsingPublicUri() {
		ResponseEntity<Void> response = restTemplate
			.withBasicAuth("Admin", "admin")
			.exchange("/orders/1000", HttpMethod.DELETE, null, Void.class);

		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
		
		// now test that the order is actually deleted
		ResponseEntity<String> getResponse = restTemplate
				.withBasicAuth("Admin", "admin")
			.getForEntity("/orders/1000", String.class);
			assertThat(getResponse.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
	}
	
	
	@Test
	@DirtiesContext
	void shouldAdminDeleteAnExistingOrderOfOthersUsingReservedUri() {
		ResponseEntity<Void> response = restTemplate
			.withBasicAuth("Admin", "admin")
			.exchange("/admin/orders/99", HttpMethod.DELETE, null, Void.class);

		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
		
		// now test that the order is actually deleted
		// STEP 1: the admin doesn't see it anymore using the reserved URI
		ResponseEntity<String> getResponseStep1 = restTemplate
				.withBasicAuth("Admin", "admin")
				.getForEntity("/admin/orders/99", String.class);
			assertThat(getResponseStep1.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);

		// STEP 2: the admin doesn't see it anymore using the public URI
		ResponseEntity<String> getResponseStep2 = restTemplate
				.withBasicAuth("Admin", "admin")
				.getForEntity("/orders/99", String.class);
			assertThat(getResponseStep2.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
			
		// STEP 3: the owner doesn't see it anymore using the public URI
		ResponseEntity<String> getResponseStep3 = restTemplate
				.withBasicAuth("Alice", "alice")
				.getForEntity("/orders/99", String.class);
			assertThat(getResponseStep3.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
	}
	
	
	@Test
	@DirtiesContext
	void shouldAdminDeleteAnExistingOrderOfOthersUsingPublicUri() {
		ResponseEntity<Void> response = restTemplate
				.withBasicAuth("Admin", "admin")
				.exchange("/orders/99", HttpMethod.DELETE, null, Void.class);

			assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
			
			// now test that the order is actually deleted
			// STEP 1: the admin doesn't see it anymore using the reserved URI
			ResponseEntity<String> getResponseStep1 = restTemplate
					.withBasicAuth("Admin", "admin")
					.getForEntity("/admin/orders/99", String.class);
				assertThat(getResponseStep1.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);

			// STEP 2: the admin doesn't see it anymore using the public URI
			ResponseEntity<String> getResponseStep2 = restTemplate
					.withBasicAuth("Admin", "admin")
					.getForEntity("/orders/99", String.class);
				assertThat(getResponseStep2.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
				
			// STEP 3: the owner doesn't see it anymore using the public URI
			ResponseEntity<String> getResponseStep3 = restTemplate
					.withBasicAuth("Alice", "alice")
					.getForEntity("/orders/99", String.class);
				assertThat(getResponseStep3.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
	}
	
	
	@Test
	void shouldNotDeleteAnOrderThatDoesNotExist() {
		ResponseEntity<Void> deleteResponse = restTemplate
			.withBasicAuth("Admin", "admin")
			.exchange("/admin/orders/99999", HttpMethod.DELETE, null, Void.class);

		assertThat(deleteResponse.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
	}
	

	@Test
	void shouldReturnToAdminAllOrdersWhenListIsRequestedUsingReservedUri() {
		ResponseEntity<String> response = restTemplate
				.withBasicAuth("Admin", "admin")
				.getForEntity("/admin/orders", String.class);
		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
		
		DocumentContext documentContext = JsonPath.parse(response.getBody());
		int ordersCount = documentContext.read("$.length()");
		assertThat(ordersCount).isEqualTo(7);
		
		JSONArray ids = documentContext.read("$..id");
		assertThat(ids).containsExactlyInAnyOrder(99, 100, 200, 300, 400, 600, 1000);
		
		JSONArray amounts = documentContext.read("$..amount");
		assertThat(amounts).containsExactlyInAnyOrder(123.99, 1100.99, 1200.99, 1300.99, 1400.99, 1600.99, 1968.05);
	}

	
	@Test
	void shouldReturnToAdminAllOrdersWhenListIsRequestedUsingPublicUri() {
		ResponseEntity<String> response = restTemplate
				.withBasicAuth("Admin", "admin")
				.getForEntity("/orders", String.class);
		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
		
		DocumentContext documentContext = JsonPath.parse(response.getBody());
		int ordersCount = documentContext.read("$.length()");
		assertThat(ordersCount).isEqualTo(7);
		
		JSONArray ids = documentContext.read("$..id");
		assertThat(ids).containsExactlyInAnyOrder(99, 100, 200, 300, 400, 600, 1000);
		
		JSONArray amounts = documentContext.read("$..amount");
		assertThat(amounts).containsExactlyInAnyOrder(123.99, 1100.99, 1200.99, 1300.99, 1400.99, 1600.99, 1968.05);
	}
	
}
