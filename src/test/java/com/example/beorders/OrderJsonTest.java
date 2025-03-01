package com.example.beorders;


import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;

import org.assertj.core.util.Arrays;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.json.JsonTest;
import org.springframework.boot.test.json.JacksonTester;
import org.springframework.boot.test.json.JsonContent;
import com.example.beorders.orders.Order;

import static org.assertj.core.api.Assertions.assertThat;

@JsonTest
public class OrderJsonTest {
	@Autowired
	private JacksonTester<Order> json;
	
	@Autowired
	private JacksonTester<Order[]> jsonList;
	
	private Order[] beOrders;
	
	
	@BeforeEach
	void setUp() {
		beOrders = Arrays.array(
				new Order( 99L,  123.99, "Alice", "Ring",      1),
				new Order(100L, 1100.99, "Alice", "Food",      1),
				new Order(200L, 1200.99, "Alice", "Motorbike", 1),
				new Order(300L, 1300.99, "Alice", "Dogfood",   1),
				new Order(400L, 1400.99, "Alice", "Fork",      1)
		);
	}

	
	@Test
	void OrderSerializationTest() throws IOException {
		Order anOrder = new Order(100L, 123.00, "Alice", "Food", 1);
		JsonContent<Order> jsonOrder = json.write(anOrder);
		
		// test write is correct
		assertThat(jsonOrder).isStrictlyEqualToJson("orders/order_expected_single.json");

		assertThat(jsonOrder).hasJsonPathNumberValue("@.id");
		assertThat(jsonOrder).extractingJsonPathNumberValue("@.id").isEqualTo(100);

		assertThat(jsonOrder).hasJsonPathNumberValue("@.amount");
		assertThat(jsonOrder).extractingJsonPathNumberValue("@.amount").isEqualTo(123.0);

		assertThat(jsonOrder).hasJsonPathStringValue("@.owner");
		assertThat(jsonOrder).extractingJsonPathStringValue("@.owner").isEqualTo("Alice");

		assertThat(jsonOrder).hasJsonPathStringValue("@.product");
		assertThat(jsonOrder).extractingJsonPathStringValue("@.product").isEqualTo("Food");
		
		assertThat(jsonOrder).hasJsonPathNumberValue("@.quantity");
		assertThat(jsonOrder).extractingJsonPathNumberValue("@.quantity").isEqualTo(1);
	}

	
	@Test
	void OrderDeserializationTest() throws IOException {
		String expected = """
			{
				"id": 100,
				"amount": 123.00,
				"owner": "Alice",
				"product": "Food",
				"quantity": 1
			}
		""";
		
		// test that parsing is correct
		assertThat(json.parse(expected)).isEqualTo(new Order(100L, 123.00, "Alice", "Food", 1));

		// test that 'Order' attributes are read correctly
		assertThat(json.parseObject(expected).id()).isEqualTo(100);
		assertThat(json.parseObject(expected).amount()).isEqualTo(123.0);
		assertThat(json.parseObject(expected).owner()).isEqualTo("Alice");
		assertThat(json.parseObject(expected).product()).isEqualTo("Food");
		assertThat(json.parseObject(expected).quantity()).isEqualTo(1);
	}
	

	@Test
	void ordersListSerializationTest() throws IOException {
		assertThat(jsonList.write(beOrders)).isStrictlyEqualToJson("orders/order_expected_list.json");
	}
	
	
	@Test
	void ordersListDeserializationTest() throws IOException {
		String expected="""
				[
					{"id":  99, "amount":  123.99, "owner": "Alice", "product": "Ring",      "quantity":1},
					{"id": 100, "amount": 1100.99, "owner": "Alice", "product": "Food",      "quantity":1},
					{"id": 200, "amount": 1200.99, "owner": "Alice", "product": "Motorbike", "quantity":1},
					{"id": 300, "amount": 1300.99, "owner": "Alice", "product": "Dogfood",   "quantity":1},
					{"id": 400, "amount": 1400.99, "owner": "Alice", "product": "Fork",      "quantity":1}
				]
				""";
		assertThat(jsonList.parse(expected)).isEqualTo(beOrders);
	}
}
