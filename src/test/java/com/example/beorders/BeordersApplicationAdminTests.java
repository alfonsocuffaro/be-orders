package com.example.beorders;

import static org.assertj.core.api.Assertions.assertThat;

import java.net.URI;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.annotation.DirtiesContext;

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
				.getForEntity("/v1/admin/orders/100", String.class);

		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
		DocumentContext documentContext = JsonPath.parse(response.getBody());
		
		Number id = documentContext.read("$.id");
		Double amount = documentContext.read("$.amount");
		String username = documentContext.read("$.owner");
		String product = documentContext.read("$.product");
		Number quantity = documentContext.read("$.quantity");
		
		assertThat(id).isEqualTo(100);
		assertThat(amount).isEqualTo(450.00);
		assertThat(username).isEqualTo("Alice");
		assertThat(product).isEqualTo("Golden Ring");
		assertThat(quantity).isEqualTo(10);
	}
	
	
	@Test
	void shouldReturnAnOrderWhenDataIsSavedUsingPublicUri() {
		ResponseEntity<String> response = restTemplate
				.withBasicAuth("Admin", "admin")
				.getForEntity("/v1/orders/100", String.class);

		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
		DocumentContext documentContext = JsonPath.parse(response.getBody());
		
		Number id = documentContext.read("$.id");
		Double amount = documentContext.read("$.amount");
		String username = documentContext.read("$.owner");
		String product = documentContext.read("$.product");
		Number quantity = documentContext.read("$.quantity");
		
		assertThat(id).isEqualTo(100);
		assertThat(amount).isEqualTo(450.00);
		assertThat(username).isEqualTo("Alice");
		assertThat(product).isEqualTo("Golden Ring");
		assertThat(quantity).isEqualTo(10);
	}
	
	
	@Test
	void shouldNotReturnAnOrderWithAnUnknownId() {
		ResponseEntity<String> response = restTemplate
				.withBasicAuth("Admin", "admin")
				.getForEntity("/v1/admin/orders/5000", String.class);

		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
		assertThat(response.getBody()).isBlank();
	}
	
	
	@Test
	@DirtiesContext
	void shouldCreateANewOrderForAdminHimself() {
		// owner parameter is null because the owner is taken from the principal
		Order newOrder = new Order(null, 250.00, null, "Computer laptop", 2);
		ResponseEntity<Void> createResponse = restTemplate
				.withBasicAuth("Admin", "admin")
				.postForEntity("/v1/admin/orders", newOrder, Void.class);
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
		String product = documentContext.read("$.product");
		Integer quantity = documentContext.read("$.quantity");

		assertThat(id).isNotNull();
		assertThat(amount).isEqualTo(250.00);
		assertThat(username).isEqualTo("Admin");
		assertThat(product).isEqualTo("Computer laptop");
		assertThat(quantity).isEqualTo(2);
	}
	
	
	@Test
	@DirtiesContext
	void shouldCreateANewOrderOnBehalfOthersUsingReservedUri() {
		// Admin creates an order on behalf of Alice
		Order newOrd = new Order(null, 250.00, "Alice", "Bicicletta", 10);
		ResponseEntity<Void> createResponse = restTemplate
				.withBasicAuth("Admin", "admin")
				.postForEntity("/v1/admin/orders", newOrd, Void.class);
		assertThat(createResponse.getStatusCode()).isEqualTo(HttpStatus.CREATED);
		
		// Verify that the update has really changed the order in the data store.
		// Verification 1: Admin should see the new order.
		URI locationOfNewOrder = createResponse.getHeaders().getLocation();
		ResponseEntity<String> getResponse = restTemplate
				.withBasicAuth("Admin", "admin")
				.getForEntity(locationOfNewOrder, String.class);
		assertThat(getResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
		
		DocumentContext documentContext = JsonPath.parse(getResponse.getBody());
		Number id = documentContext.read("$.id");
		Double amount = documentContext.read("$.amount");
		String owner = documentContext.read("$.owner");
		String product = documentContext.read("$.product");
		Integer quantity = documentContext.read("$.quantity");

		assertThat(id).isNotNull();
		assertThat(amount).isEqualTo(250.00);
		assertThat(owner).isEqualTo("Alice");
		assertThat(product).isEqualTo("Bicicletta");
		assertThat(quantity).isEqualTo(10);
		
		// Verification 2: also Alice (the order's owner) should see it under the URI "/v1/orders/{id}".
		// The order has been created by the Admin so the system places in the location
		// header the following URL 'http://localhost:50449/v1/admin/orders/1'.
		// This URL is not directly accessible to Alice (it's under /v1/admin/*), so the location header
		// must be reworked to eliminate the '/admin/' part.
		// example:
		// - URI from location header: http://localhost:50449/v1/admin/orders/1
		// - URI for Alice: http://localhost:50449/v1/orders/1
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
		String productFromOwnerResponse = documentContext.read("$.product");
		Integer quantityFromOwnerResponse = documentContext.read("$.quantity");

		assertThat(idFromOwnerResponse).isNotNull();
		assertThat(amountFromOwnerResponse).isEqualTo(250.00);
		assertThat(ownerFromOwnerResponse).isEqualTo("Alice");
		assertThat(productFromOwnerResponse).isEqualTo("Bicicletta");
		assertThat(quantityFromOwnerResponse).isEqualTo(10);
	}
	
	
	@Test
	@DirtiesContext
	void shouldCreateANewOrderOnBehalfOthersUsingPublicUri() {
		// Admin creates an order on behalf of Alice
		Order newOrd = new Order(null, 250.00, "Alice", "Bicicletta", 10);
		ResponseEntity<Void> createResponse = restTemplate
				.withBasicAuth("Admin", "admin")
				.postForEntity("/v1/orders", newOrd, Void.class);
		assertThat(createResponse.getStatusCode()).isEqualTo(HttpStatus.CREATED);
		
		// Verify that the update has really changed the order in the data store.
		// Verification 1: Admin should see the new order.
		URI locationOfNewOrder = createResponse.getHeaders().getLocation();
		ResponseEntity<String> getResponse = restTemplate
				.withBasicAuth("Admin", "admin")
				.getForEntity(locationOfNewOrder, String.class);
		assertThat(getResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
		
		DocumentContext documentContext = JsonPath.parse(getResponse.getBody());
		Number id = documentContext.read("$.id");
		Double amount = documentContext.read("$.amount");
		String owner = documentContext.read("$.owner");
		String product = documentContext.read("$.product");
		Integer quantity = documentContext.read("$.quantity");

		assertThat(id).isNotNull();
		assertThat(amount).isEqualTo(250.00);
		assertThat(owner).isEqualTo("Alice");
		assertThat(product).isEqualTo("Bicicletta");
		assertThat(quantity).isEqualTo(10);
		
		// Verification 2: also Alice (the order's owner) should see it under the URI "/v1/orders/{id}".
		// The order has been created by the Admin so the system places in the location
		// header the following URL 'http://localhost:50449/v1/admin/orders/1'.
		// This URL is not directly accessible to Alice (it's under /v1/admin/*), so the location header
		// must be reworked to eliminate the '/admin/' part.
		// example:
		// - URI from location header: http://localhost:50449/v1/admin/orders/1
		// - URI for Alice: http://localhost:50449/v1/orders/1
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
		String productFromOwnerResponse = documentContext.read("$.product");
		Integer quantityFromOwnerResponse = documentContext.read("$.quantity");

		assertThat(idFromOwnerResponse).isNotNull();
		assertThat(amountFromOwnerResponse).isEqualTo(250.00);
		assertThat(ownerFromOwnerResponse).isEqualTo("Alice");
		assertThat(productFromOwnerResponse).isEqualTo("Bicicletta");
		assertThat(quantityFromOwnerResponse).isEqualTo(10);
	}

	
	@Test
	void shouldReturnAllOrdersOfEveryoneWhenListIsRequestedUsingReservedUri() {
		ResponseEntity<String> response = restTemplate
				.withBasicAuth("Admin", "admin")
				.getForEntity("/v1/admin/orders", String.class);
		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
		
		DocumentContext documentContext = JsonPath.parse(response.getBody());
		int ordersCount = documentContext.read("$.length()");
		JSONArray ids = documentContext.read("$..id");
		JSONArray amounts = documentContext.read("$..amount");
		JSONArray owners = documentContext.read("$..owner");
		JSONArray products = documentContext.read("$..product");
		JSONArray quantities = documentContext.read("$..quantity");
		
		assertThat(ordersCount).isEqualTo(9);
		assertThat(ids).containsExactlyInAnyOrder(50, 100, 105, 110, 200, 300, 400, 600, 1000);
		assertThat(amounts).containsExactlyInAnyOrder(1100.99, 450.00, 500.50, 250.00, 1200.99, 1300.99, 1400.99, 1600.99, 1968.05);
		assertThat(owners).containsExactlyInAnyOrder("Alice", "Alice", "Alice", "Alice", "Alice", "Alice", "Alice", "Cathy", "Admin");
		assertThat(products).containsExactlyInAnyOrder("Food", "Golden Ring", "Ring with diamonds",
				"Ring", "Motorbike", "Dogfood", "Fork", "Dogfood", "Computer");
		assertThat(quantities).containsExactlyInAnyOrder(10, 10, 5, 1, 1, 1, 1, 1, 1);
	}

	
	@Test
	void shouldReturnAllOrdersOfEveryoneWhenListIsRequestedUsingPublicUri() {
		ResponseEntity<String> response = restTemplate
				.withBasicAuth("Admin", "admin")
				.getForEntity("/v1/orders", String.class);
		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
		
		DocumentContext documentContext = JsonPath.parse(response.getBody());
		int ordersCount = documentContext.read("$.length()");
		JSONArray ids = documentContext.read("$..id");
		JSONArray amounts = documentContext.read("$..amount");
		JSONArray owners = documentContext.read("$..owner");
		JSONArray products = documentContext.read("$..product");
		JSONArray quantities = documentContext.read("$..quantity");
		
		assertThat(ordersCount).isEqualTo(9);
		assertThat(ids).containsExactlyInAnyOrder(50, 100, 105, 110, 200, 300, 400, 600, 1000);
		assertThat(amounts).containsExactlyInAnyOrder(1100.99, 450.00, 500.50, 250.00, 1200.99, 1300.99, 1400.99, 1600.99, 1968.05);
		assertThat(owners).containsExactlyInAnyOrder("Alice", "Alice", "Alice", "Alice", "Alice", "Alice", "Alice", "Cathy", "Admin");
		assertThat(products).containsExactlyInAnyOrder("Food", "Golden Ring", "Ring with diamonds",
				"Ring", "Motorbike", "Dogfood", "Fork", "Dogfood", "Computer");
		assertThat(quantities).containsExactlyInAnyOrder(10, 10, 5, 1, 1, 1, 1, 1, 1);
	}
	
	
	@Test
	void shouldReturnAllOrdersOfEveryoneHavingASpecificProductTypeWhenListIsRequestedUsingReservedUri() {
		ResponseEntity<String> response = restTemplate
				.withBasicAuth("Admin", "admin")
				.getForEntity("/v1/admin/orders?productType=Dogfood", String.class);
		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
		
		DocumentContext documentContext = JsonPath.parse(response.getBody());
		int ordersCount = documentContext.read("$.length()");
		JSONArray ids = documentContext.read("$..id");
		JSONArray amounts = documentContext.read("$..amount");
		JSONArray owners = documentContext.read("$..owner");
		JSONArray products = documentContext.read("$..product");
		JSONArray quantities = documentContext.read("$..quantity");
		
		assertThat(ordersCount).isEqualTo(2);
		assertThat(ids).containsExactlyInAnyOrder(300, 600);
		assertThat(amounts).containsExactlyInAnyOrder(1300.99, 1600.99);
		assertThat(owners).containsExactlyInAnyOrder("Alice", "Cathy");
		assertThat(products).containsExactlyInAnyOrder("Dogfood", "Dogfood");
		assertThat(quantities).containsExactlyInAnyOrder(1, 1);
	}

	
	
	@Test
	void shouldReturnAllOrdersOfEveryoneHavingASpecificProductTypeIgnoringCaseWhenListIsRequestedUsingReservedUri() {
		ResponseEntity<String> response = restTemplate
				.withBasicAuth("Admin", "admin")
				.getForEntity("/v1/admin/orders?productType=dogfood", String.class);
		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
		
		DocumentContext documentContext = JsonPath.parse(response.getBody());
		int ordersCount = documentContext.read("$.length()");
		JSONArray ids = documentContext.read("$..id");
		JSONArray amounts = documentContext.read("$..amount");
		JSONArray owners = documentContext.read("$..owner");
		JSONArray products = documentContext.read("$..product");
		JSONArray quantities = documentContext.read("$..quantity");
		
		assertThat(ordersCount).isEqualTo(2);
		assertThat(ids).containsExactlyInAnyOrder(300, 600);
		assertThat(amounts).containsExactlyInAnyOrder(1300.99, 1600.99);
		assertThat(owners).containsExactlyInAnyOrder("Alice", "Cathy");
		assertThat(products).containsExactlyInAnyOrder("Dogfood", "Dogfood");
		assertThat(quantities).containsExactlyInAnyOrder(1, 1);
	}
	
	@Test
	void shouldReturnNoOrdersHavingANotExisistingProductTypeWhenListIsRequestedUsingPublicUri() {
		ResponseEntity<String> response = restTemplate
				.withBasicAuth("Admin", "admin")
				.getForEntity("/v1/orders?productType=FakeProductType", String.class);
		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
		
		DocumentContext documentContext = JsonPath.parse(response.getBody());
		int ordersCount = documentContext.read("$.length()");
		assertThat(ordersCount).isZero();
	}
	
	
	@Test
	@DirtiesContext
	void shouldUpdateAnExistingOrderOfAdminUsingReservedUri() {
		Order orderUpdate = new Order(null, 200.00, "Admin", "Computer quantistico", 5);
		HttpEntity<Order> request = new HttpEntity<>(orderUpdate);
		ResponseEntity<Void> response = restTemplate
				.withBasicAuth("Admin", "admin")
				.exchange("/v1/admin/orders/1000", HttpMethod.PUT, request, Void.class);
		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
		
		// check that the 'update' operation has really changed the order in the data store.
		ResponseEntity<String> responseToGet = restTemplate
				.withBasicAuth("Admin", "admin")
				.getForEntity("/v1/admin/orders/1000", String.class);
		assertThat(responseToGet.getStatusCode()).isEqualTo(HttpStatus.OK);
		DocumentContext documentContext = JsonPath.parse(responseToGet.getBody());
		Number id = documentContext.read("$.id");
		Double amount = documentContext.read("$.amount");
		String owner = documentContext.read("$.owner");
		String product = documentContext.read("$.product");
		Integer quantity = documentContext.read("$.quantity");
		
		assertThat(id).isEqualTo(1000);
		assertThat(amount).isEqualTo(200.00);
		assertThat(owner).isEqualTo("Admin");
		assertThat(product).isEqualTo("Computer quantistico");
		assertThat(quantity).isEqualTo(5);
	}
	
	
	@Test
	@DirtiesContext
	void shouldUpdateAnExistingOrderOfAdminUsingPublicUri() {
		Order orderUpdate = new Order(null, 200.00, "Admin", "Computer quantistico", 5);
		HttpEntity<Order> request = new HttpEntity<>(orderUpdate);
		ResponseEntity<Void> response = restTemplate
				.withBasicAuth("Admin", "admin")
				.exchange("/v1/orders/1000", HttpMethod.PUT, request, Void.class);
		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);

		// check that the 'update' operation has really changed the order in the data store.
		ResponseEntity<String> responseToGet = restTemplate
				.withBasicAuth("Admin", "admin")
				.getForEntity("/v1/orders/1000", String.class);
		assertThat(responseToGet.getStatusCode()).isEqualTo(HttpStatus.OK);
		DocumentContext documentContext = JsonPath.parse(responseToGet.getBody());
		Number id = documentContext.read("$.id");
		Double amount = documentContext.read("$.amount");
		String owner = documentContext.read("$.owner");
		String product = documentContext.read("$.product");
		Integer quantity = documentContext.read("$.quantity");
		
		assertThat(id).isEqualTo(1000);
		assertThat(amount).isEqualTo(200.00);
		assertThat(owner).isEqualTo("Admin");
		assertThat(product).isEqualTo("Computer quantistico");
		assertThat(quantity).isEqualTo(5);
	}
	
	
	@Test
	@DirtiesContext
	void shouldUpdateAnExistingOrderOnBehalfOfOthersUsingReservedUri() {
		Order orderUpdate = new Order(null, 200.00, "Alice", "Computer quantistico", 5);
		HttpEntity<Order> request = new HttpEntity<>(orderUpdate);
		ResponseEntity<Void> response = restTemplate
				.withBasicAuth("Admin", "admin")
				.exchange("/v1/admin/orders/100", HttpMethod.PUT, request, Void.class);
		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);

		// check that the 'update' operation has really changed the order in the data store.
		// Verification 1: Admin should see the new order.
		ResponseEntity<String> responseToGet = restTemplate
				.withBasicAuth("Admin", "admin")
				.getForEntity("/v1/admin/orders/100", String.class);
		
		assertThat(responseToGet.getStatusCode()).isEqualTo(HttpStatus.OK);
		DocumentContext documentContext = JsonPath.parse(responseToGet.getBody());
		Number id = documentContext.read("$.id");
		Double amount = documentContext.read("$.amount");
		String owner = documentContext.read("$.owner");
		String product = documentContext.read("$.product");
		Integer quantity = documentContext.read("$.quantity");
		
		assertThat(id).isEqualTo(100);
		assertThat(amount).isEqualTo(200.00);
		assertThat(owner).isEqualTo("Alice");
		assertThat(product).isEqualTo("Computer quantistico");
		assertThat(quantity).isEqualTo(5);
	}
	
	
	@Test
	@DirtiesContext
	void shouldUpdateAnExistingOrderOnBehalfOfOthersUsingPublicUri() {
		Order orderUpdate = new Order(null, 200.00, "Alice", "Computer quantistico", 5);
		HttpEntity<Order> request = new HttpEntity<>(orderUpdate);
		ResponseEntity<Void> response = restTemplate
				.withBasicAuth("Admin", "admin")
				.exchange("/v1/orders/100", HttpMethod.PUT, request, Void.class);
		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
		
		// Verify that the update has really changed the order in the data store.
		// Verification 1: Admin should see the new order.
		ResponseEntity<String> responseToGet = restTemplate
				.withBasicAuth("Admin", "admin")
				.getForEntity("/v1/orders/100", String.class);
		
		assertThat(responseToGet.getStatusCode()).isEqualTo(HttpStatus.OK);
		DocumentContext documentContext = JsonPath.parse(responseToGet.getBody());
		Number id = documentContext.read("$.id");
		Double amount = documentContext.read("$.amount");
		String owner = documentContext.read("$.owner");
		String product = documentContext.read("$.product");
		Integer quantity = documentContext.read("$.quantity");
		
		assertThat(id).isEqualTo(100);
		assertThat(amount).isEqualTo(200.00);
		assertThat(owner).isEqualTo("Alice");
		assertThat(product).isEqualTo("Computer quantistico");
		assertThat(quantity).isEqualTo(5);
	}

	
	@Test
	void shouldReturnAPageOfOrdersUsingReservedUri() {
		ResponseEntity<String> response = restTemplate
				.withBasicAuth("Admin", "admin")
				.getForEntity("/v1/admin/orders?page=0&size=1", String.class);
		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);

		DocumentContext documentContext = JsonPath.parse(response.getBody());
		JSONArray page = documentContext.read("$[*]");
		assertThat(page).hasSize(1);
	}
	
	
	@Test
	void shouldReturnAPageOfOrdersUsingPublicUri() {
		ResponseEntity<String> response = restTemplate
				.withBasicAuth("Admin", "admin")
				.getForEntity("/v1/orders?page=0&size=1", String.class);
		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);

		DocumentContext documentContext = JsonPath.parse(response.getBody());
		JSONArray page = documentContext.read("$[*]");
		assertThat(page).hasSize(1);
	}
	
	
	@Test
	void shouldReturnASortedPageOfOrdersUsingReservedUri() {
		ResponseEntity<String> response = restTemplate
				.withBasicAuth("Admin", "admin")
				.getForEntity("/v1/admin/orders?page=0&size=1&sort=amount,desc", String.class);
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
				.getForEntity("/v1/orders?page=0&size=1&sort=amount,desc", String.class);
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
				.getForEntity("/v1/admin/orders", String.class);
		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);

		DocumentContext documentContext = JsonPath.parse(response.getBody());
		JSONArray page = documentContext.read("$[*]");
		JSONArray ids = documentContext.read("$..id");
		JSONArray amounts = documentContext.read("$..amount");
		JSONArray owners = documentContext.read("$..owner");
		JSONArray products = documentContext.read("$..product");
		JSONArray quantities = documentContext.read("$..quantity");

		assertThat(page).hasSize(9);
		assertThat(ids).containsExactlyInAnyOrder(50, 100, 105, 110, 200, 300, 400, 600, 1000);
		assertThat(amounts).containsExactlyInAnyOrder(1100.99, 450.00, 500.50, 250.00, 1200.99, 1300.99, 1400.99, 1600.99, 1968.05);
		assertThat(owners).containsExactlyInAnyOrder("Alice", "Alice", "Alice", "Alice", "Alice", "Alice", "Alice", "Cathy", "Admin");
		assertThat(products).containsExactlyInAnyOrder("Food", "Golden Ring", "Ring with diamonds",
				"Ring", "Motorbike", "Dogfood", "Fork", "Dogfood", "Computer");
		assertThat(quantities).containsExactlyInAnyOrder(10, 10, 5, 1, 1, 1, 1, 1, 1);
	}
	
	
	@Test
	void shouldNotReturnAnOrderWhenUsingBadUsername() {
	    ResponseEntity<String> response = restTemplate
	      .withBasicAuth("BAD_USERNAME", "alice")
	      .getForEntity("/v1/admin/orders/99", String.class);
	    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
	}
	
	
	@Test
	void shouldNotReturnAnOrderWhenUsingBadPassword() {
	    ResponseEntity<String> response = restTemplate
	      .withBasicAuth("Admin", "BAD_PASSWORD")
	      .getForEntity("/v1/orders/99", String.class);
	    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
	}
	
	
	@Test
	void shouldNotReturnAnOrderWhenUsingBadCredentials() {
	    ResponseEntity<String> response = restTemplate
	      .withBasicAuth("BAD_USER", "BAD_PASSWORD")
	      .getForEntity("/v1/admin/orders/99", String.class);
	    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
	}
	
	
	@Test
	void shouldNotUpdateAnOrderThatDoesNotExistUsingReservedUri() {
		Order unknownOrder = new Order(null, 19.99, null, null, null);
		HttpEntity<Order> request = new HttpEntity<>(unknownOrder);
		ResponseEntity<Void> response = restTemplate
				.withBasicAuth("Admin", "admin")
				.exchange("/v1/admin/orders/99999", HttpMethod.PUT, request, Void.class);
	
		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
	}
	
	
	@Test
	void shouldNotUpdateAnOrderThatDoesNotExistUsingPublicUri() {
		Order unknownOrder = new Order(null, 19.99, null, null, null);
		HttpEntity<Order> request = new HttpEntity<>(unknownOrder);
		ResponseEntity<Void> response = restTemplate
				.withBasicAuth("Admin", "admin")
				.exchange("/v1/orders/99999", HttpMethod.PUT, request, Void.class);
	
		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
	}

	
	@Test
	@DirtiesContext
	void shouldAdminDeleteAnExistingOrderOfAdminUsingReservedUri() {
		ResponseEntity<Void> response = restTemplate
			.withBasicAuth("Admin", "admin")
			.exchange("/v1/admin/orders/1000", HttpMethod.DELETE, null, Void.class);

		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
		
		// now test that the order is actually deleted
		ResponseEntity<String> getResponse = restTemplate
				.withBasicAuth("Admin", "admin")
			.getForEntity("/v1/admin/orders/1000", String.class);
			assertThat(getResponse.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
	}
	
	
	@Test
	@DirtiesContext
	void shouldAdminDeleteAnExistingOrderOfAdminUsingPublicUri() {
		ResponseEntity<Void> response = restTemplate
			.withBasicAuth("Admin", "admin")
			.exchange("/v1/orders/1000", HttpMethod.DELETE, null, Void.class);

		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
		
		// now test that the order is actually deleted
		ResponseEntity<String> getResponse = restTemplate
				.withBasicAuth("Admin", "admin")
			.getForEntity("/v1/orders/1000", String.class);
			assertThat(getResponse.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
	}
	
	
	@Test
	@DirtiesContext
	void shouldAdminDeleteAnExistingOrderOfOthersUsingReservedUri() {
		ResponseEntity<Void> response = restTemplate
			.withBasicAuth("Admin", "admin")
			.exchange("/v1/admin/orders/100", HttpMethod.DELETE, null, Void.class);

		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
		
		// Now verify that the order is actually deleted.
		// STEP 1: the Admin doesn't see it anymore using the reserved URI.
		ResponseEntity<String> getResponseStep1 = restTemplate
				.withBasicAuth("Admin", "admin")
				.getForEntity("/v1/admin/orders/100", String.class);
			assertThat(getResponseStep1.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);

		// STEP 2: the Admin doesn't see it anymore using the public URI
		ResponseEntity<String> getResponseStep2 = restTemplate
				.withBasicAuth("Admin", "admin")
				.getForEntity("/v1/orders/100", String.class);
			assertThat(getResponseStep2.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
			
		// STEP 3: the owner doesn't see it anymore using the public URI
		ResponseEntity<String> getResponseStep3 = restTemplate
				.withBasicAuth("Alice", "alice")
				.getForEntity("/v1/orders/100", String.class);
			assertThat(getResponseStep3.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
	}
	
	
	@Test
	@DirtiesContext
	void shouldAdminDeleteAnExistingOrderOfOthersUsingPublicUri() {
		ResponseEntity<Void> response = restTemplate
				.withBasicAuth("Admin", "admin")
				.exchange("/v1/orders/300", HttpMethod.DELETE, null, Void.class);

			assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
			
			// Now verify that the order is actually deleted.
			// STEP 1: the Admin doesn't see it anymore using the reserved URI.
			ResponseEntity<String> getResponseStep1 = restTemplate
					.withBasicAuth("Admin", "admin")
					.getForEntity("/v1/admin/orders/300", String.class);
				assertThat(getResponseStep1.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);

				// STEP 2: the Admin doesn't see it anymore using the public URI
			ResponseEntity<String> getResponseStep2 = restTemplate
					.withBasicAuth("Admin", "admin")
					.getForEntity("/v1/orders/300", String.class);
				assertThat(getResponseStep2.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
				
				// STEP 3: the owner doesn't see it anymore using the public URI
			ResponseEntity<String> getResponseStep3 = restTemplate
					.withBasicAuth("Alice", "alice")
					.getForEntity("/v1/orders/300", String.class);
				assertThat(getResponseStep3.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
	}
	
	
	@Test
	void shouldNotDeleteAnOrderThatDoesNotExist() {
		ResponseEntity<Void> deleteResponse = restTemplate
			.withBasicAuth("Admin", "admin")
			.exchange("/v1/admin/orders/99999", HttpMethod.DELETE, null, Void.class);

		assertThat(deleteResponse.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
	}
	

	@Test
	void shouldReturnToAdminAllOrdersWhenListIsRequestedUsingReservedUri() {
		ResponseEntity<String> response = restTemplate
				.withBasicAuth("Admin", "admin")
				.getForEntity("/v1/admin/orders", String.class);
		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
		
		DocumentContext documentContext = JsonPath.parse(response.getBody());
		int ordersCount = documentContext.read("$.length()");
		JSONArray ids = documentContext.read("$..id");
		JSONArray amounts = documentContext.read("$..amount");
		JSONArray owners = documentContext.read("$..owner");
		JSONArray products = documentContext.read("$..product");
		JSONArray quantities = documentContext.read("$..quantity");
		
		assertThat(ordersCount).isEqualTo(9);
		assertThat(ids).containsExactlyInAnyOrder(50, 100, 105, 110, 200, 300, 400, 600, 1000);
		assertThat(amounts).containsExactlyInAnyOrder(1100.99, 450.00, 500.50, 250.00, 1200.99, 1300.99, 1400.99, 1600.99, 1968.05);
		assertThat(owners).containsExactlyInAnyOrder("Alice", "Alice", "Alice", "Alice", "Alice", "Alice", "Alice", "Cathy", "Admin");
		assertThat(products).containsExactlyInAnyOrder("Food", "Golden Ring", "Ring with diamonds",
				"Ring", "Motorbike", "Dogfood", "Fork", "Dogfood", "Computer");
		assertThat(quantities).containsExactlyInAnyOrder(10, 10, 5, 1, 1, 1, 1, 1, 1);
	}

	
	@Test
	void shouldReturnToAdminAllOrdersWhenListIsRequestedUsingPublicUri() {
		ResponseEntity<String> response = restTemplate
				.withBasicAuth("Admin", "admin")
				.getForEntity("/v1/orders", String.class);
		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
		
		DocumentContext documentContext = JsonPath.parse(response.getBody());
		int ordersCount = documentContext.read("$.length()");
		JSONArray ids = documentContext.read("$..id");
		JSONArray amounts = documentContext.read("$..amount");
		JSONArray owners = documentContext.read("$..owner");
		JSONArray products = documentContext.read("$..product");
		JSONArray quantities = documentContext.read("$..quantity");
		
		assertThat(ordersCount).isEqualTo(9);
		assertThat(ids).containsExactlyInAnyOrder(50, 100, 105, 110, 200, 300, 400, 600, 1000);
		assertThat(amounts).containsExactlyInAnyOrder(1100.99, 450.00, 500.50, 250.00, 1200.99, 1300.99, 1400.99, 1600.99, 1968.05);
		assertThat(owners).containsExactlyInAnyOrder("Alice", "Alice", "Alice", "Alice", "Alice", "Alice", "Alice", "Cathy", "Admin");
		assertThat(products).containsExactlyInAnyOrder("Food", "Golden Ring", "Ring with diamonds",
				"Ring", "Motorbike", "Dogfood", "Fork", "Dogfood", "Computer");
		assertThat(quantities).containsExactlyInAnyOrder(10, 10, 5, 1, 1, 1, 1, 1, 1);
	}
	
}
