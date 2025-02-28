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
class BeordersApplicationTests {
	
	@Autowired
	TestRestTemplate restTemplate;

	@Test
	void contextLoads() {
	}

	
	@Test
	void shouldReturnAnOrderWhenDataIsSaved() {
		ResponseEntity<String> response = restTemplate
				.withBasicAuth("Alice", "alice")
				.getForEntity("/orders/99", String.class);

		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
		DocumentContext documentContext = JsonPath.parse(response.getBody());
		
		Number id = documentContext.read("$.id");
		assertThat(id).isEqualTo(99);
		
		Double amount = documentContext.read("$.amount");
		assertThat(amount).isEqualTo(123.99);

		String username = documentContext.read("$.owner");
		assertThat(username).isEqualTo("Alice");
	}
	
	
	@Test
	void shouldNotReturnAnOrderWithAnUnknownId() {
		ResponseEntity<String> response = restTemplate
				.withBasicAuth("Alice", "alice")
				.getForEntity("/orders/9999", String.class);

		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
		assertThat(response.getBody()).isBlank();
	}
	
	
	@Test
	@DirtiesContext
	void shouldCreateANewOrder() {
		// owner parameter is null because the owner is taken from the principal 
		Order newOrd = new Order(null, 250.00, null);
		ResponseEntity<Void> createResponse = restTemplate
				.withBasicAuth("Alice", "alice")
				.postForEntity("/orders", newOrd, Void.class);
		assertThat(createResponse.getStatusCode()).isEqualTo(HttpStatus.CREATED);
		
		URI locationOfNewOrder = createResponse.getHeaders().getLocation();
		ResponseEntity<String> getResponse = restTemplate
				.withBasicAuth("Alice", "alice")
				.getForEntity(locationOfNewOrder, String.class);
		assertThat(getResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
		
		DocumentContext documentContext = JsonPath.parse(getResponse.getBody());
		Number id = documentContext.read("$.id");
		Double amount = documentContext.read("$.amount");
		String owner = documentContext.read("$.owner");

		assertThat(id).isNotNull();
		assertThat(amount).isEqualTo(250.00);
		assertThat(owner).isEqualTo("Alice");
	}

	
	@Test
	void shouldReturnAllOrdersWhenListIsRequested() {
		ResponseEntity<String> response = restTemplate
				.withBasicAuth("Alice", "alice")
				.getForEntity("/orders", String.class);
		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
		
		DocumentContext documentContext = JsonPath.parse(response.getBody());
		int ordersCount = documentContext.read("$.length()");
		assertThat(ordersCount).isEqualTo(5);
		
		JSONArray ids = documentContext.read("$..id");
		JSONArray amounts = documentContext.read("$..amount");
		
		assertThat(ids).containsExactlyInAnyOrder(99, 100, 200, 300, 400);
		assertThat(amounts).containsExactlyInAnyOrder(123.99, 1100.99, 1200.99, 1300.99, 1400.99);

		// TODO testare anche lo username ????
	}
	
	
	@Test
	@DirtiesContext
	void shouldUpdateAnExistingOrder() {
		Order orderUpdate = new Order(null, 200.00, "Alice");
		HttpEntity<Order> request = new HttpEntity<>(orderUpdate);
		ResponseEntity<Void> response = restTemplate
				.withBasicAuth("Alice", "alice")
				.exchange("/orders/99", HttpMethod.PUT, request, Void.class);
		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
		
		// check that update has really changed the data store
		ResponseEntity<String> responseToGet = restTemplate
				.withBasicAuth("Alice", "alice")
				.getForEntity("/orders/99", String.class);
		assertThat(responseToGet.getStatusCode()).isEqualTo(HttpStatus.OK);
		DocumentContext documentContext = JsonPath.parse(responseToGet.getBody());
		Number id = documentContext.read("$.id");
		Double amount = documentContext.read("$.amount");
		assertThat(id).isEqualTo(99);
		assertThat(amount).isEqualTo(200.00);

		// TODO testare anche lo username ????
	}
	
	
	@Test
	void shouldReturnAPageOfOrders() {
		ResponseEntity<String> response = restTemplate
				.withBasicAuth("Alice", "alice")
				.getForEntity("/orders?page=0&size=1", String.class);
		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);

		DocumentContext documentContext = JsonPath.parse(response.getBody());
		JSONArray page = documentContext.read("$[*]");
		assertThat(page.size()).isEqualTo(1);
	}
	
	
	@Test
	void shouldReturnASortedPageOfOrders() {
		ResponseEntity<String> response = restTemplate
				.withBasicAuth("Alice", "alice")
				.getForEntity("/orders?page=0&size=1&sort=amount,desc", String.class);
		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);

		DocumentContext documentContext = JsonPath.parse(response.getBody());
		JSONArray read = documentContext.read("$[*]");
		double amount = documentContext.read("$[0].amount");
		String owner = documentContext.read("$[0].owner");
		
		assertThat(read.size()).isEqualTo(1);
		assertThat(amount).isEqualTo(1400.99);
		assertThat(owner).isEqualTo("Alice");
	}
	
	
	@Test
	void shouldReturnASortedPageOfOrdersWithNoParametersAndUseDefaultValues() {
		ResponseEntity<String> response = restTemplate
				.withBasicAuth("Alice", "alice")
				.getForEntity("/orders", String.class);
		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);

		DocumentContext documentContext = JsonPath.parse(response.getBody());
		
		JSONArray page = documentContext.read("$[*]");
		JSONArray amounts = documentContext.read("$..amount");
		JSONArray owners = documentContext.read("$..owner");
		
		assertThat(page.size()).isEqualTo(5);
		assertThat(amounts).containsExactly(123.99, 1100.99, 1200.99, 1300.99, 1400.99);
		assertThat(owners).containsExactly("Alice", "Alice", "Alice", "Alice", "Alice");
	}
	
	
	@Test
	void shouldNotReturnAnOrderWhenUsingBadUsername() {
	    ResponseEntity<String> response = restTemplate
	      .withBasicAuth("BAD_USERNAME", "alice")
	      .getForEntity("/orders/99", String.class);
	    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
	}
	
	
	@Test
	void shouldNotReturnAnOrderWhenUsingBadPassword() {
	    ResponseEntity<String> response = restTemplate
	      .withBasicAuth("Alice", "BAD_PASSWORD")
	      .getForEntity("/orders/99", String.class);
	    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
	}
	
	
	@Test
	void shouldNotReturnAnOrderWhenUsingBadCredentials() {
	    ResponseEntity<String> response = restTemplate
	      .withBasicAuth("BAD_USER", "BAD_PASSWORD")
	      .getForEntity("/orders/99", String.class);
	    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
	}
	
	
	@Test
	void shouldRejectUsersWhoAreNotOrderOwners() {
		ResponseEntity<String> response = restTemplate
			.withBasicAuth("Boris", "boris")
			.getForEntity("/orders/99", String.class);
		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
	}
	
	
	@Test
	void shouldNotAllowAccessToOrdersTheyDoNotOwn() {
		ResponseEntity<String> response = restTemplate
		.withBasicAuth("Alice", "alice")
		.getForEntity("/orders/600", String.class); // Cathy's data
		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
	}
	
	
	@Test
	void shouldNotUpdateAnOrderThatDoesNotExist() {
		Order unknownOrder = new Order(null, 19.99, null);
		HttpEntity<Order> request = new HttpEntity<>(unknownOrder);
		ResponseEntity<Void> response = restTemplate
				.withBasicAuth("Alice", "alice")
				.exchange("/orders/99999", HttpMethod.PUT, request, Void.class);
	
		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
	}
	
	
	@Test
	void shouldNotUpdateAnOrderThatIsOwnedBySomeoneElse() {
		Order cathysOrder = new Order(null, 333.33, null);
		HttpEntity<Order> request = new HttpEntity<>(cathysOrder);
		ResponseEntity<Void> response = restTemplate
				.withBasicAuth("Alice", "alice")
		.exchange("/orders/600", HttpMethod.PUT, request, Void.class);
		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
	}
	
	
	@Test
	@DirtiesContext
	void shouldDeleteAnExistingOrder() {
		ResponseEntity<Void> response = restTemplate
		.withBasicAuth("Alice", "alice")
		.exchange("/orders/99", HttpMethod.DELETE, null, Void.class);

		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
		
		
		// now test that the order is actually deleted
		ResponseEntity<String> getResponse = restTemplate
				.withBasicAuth("Alice", "alice")
				.getForEntity("/orders/99", String.class);
			assertThat(getResponse.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
		
	}
	
	
	@Test
	void shouldNotDeleteAnOrderThatDoesNotExist() {
		ResponseEntity<Void> deleteResponse = restTemplate
			.withBasicAuth("Alice", "alice")
			.exchange("/orders/99999", HttpMethod.DELETE, null, Void.class);

		assertThat(deleteResponse.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
	}
	
	
	@Test
	void shouldNotAllowDeletionOfOrdersTheyDoNotOwn() {
		ResponseEntity<Void> deleteResponse = restTemplate
			.withBasicAuth("Alice", "alice")
			.exchange("/orders/600", HttpMethod.DELETE, null, Void.class);
		
		assertThat(deleteResponse.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
		
		// verify that the record we tried unsuccessfully to delete is still there
		// order with id 600 is owned by Cathy, so we must use Cathy to try access it
		ResponseEntity<String> getResponse = restTemplate
				.withBasicAuth("Cathy", "cathy")
				.getForEntity("/orders/600", String.class);
		assertThat(getResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
		
	}
	
//	
//	@Test
//	void shouldReturnToAdminAllOrdersWhenListIsRequested() {
//		ResponseEntity<String> response = restTemplate
//				.withBasicAuth("Admin", "admin")
//				.getForEntity("/orders", String.class);
//		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
//		
//		DocumentContext documentContext = JsonPath.parse(response.getBody());
//		int ordersCount = documentContext.read("$.length()");
//		assertThat(ordersCount).isEqualTo(6);
//		
//		JSONArray ids = documentContext.read("$..id");
//		assertThat(ids).containsExactlyInAnyOrder(99, 100, 200, 300, 400, 600);
//		
//		JSONArray amounts = documentContext.read("$..amount");
//		assertThat(amounts).containsExactlyInAnyOrder(123.99, 1100.99, 1200.99, 1300.99, 1400.99, 1600.99);
//	}
	
}
