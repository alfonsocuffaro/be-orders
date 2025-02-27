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
import com.example.beorders.orders.BEOrder;

import static org.assertj.core.api.Assertions.assertThat;

@JsonTest
public class OrderJsonTest {
	@Autowired
	private JacksonTester<BEOrder> json;
	
	@Autowired
	private JacksonTester<BEOrder[]> jsonList;
	
	private BEOrder[] beOrders;
	
	
	@BeforeEach
	void setUp() {
		beOrders = Arrays.array(
				new BEOrder( 99L,  123.99, "Alice"),
				new BEOrder(100L, 1100.99, "Alice"),
				new BEOrder(200L, 1200.99, "Alice"),
				new BEOrder(300L, 1300.99, "Alice"),
				new BEOrder(400L, 1400.99, "Alice")
		);
	}

	
	@Test
	void OrderSerializationTest() throws IOException {
		BEOrder anOrder = new BEOrder(100L, 123.00, "Alice");
		JsonContent<BEOrder> jsonOrder = json.write(anOrder);
		
		// test write is correct
		assertThat(jsonOrder).isStrictlyEqualToJson("orders/order_expected_single.json");
		
		// test id is 100
		assertThat(jsonOrder).hasJsonPathNumberValue("@.id");
		assertThat(jsonOrder).extractingJsonPathNumberValue("@.id").isEqualTo(100);

		// test amount is also 100
		assertThat(jsonOrder).hasJsonPathNumberValue("@.amount");
		assertThat(jsonOrder).extractingJsonPathNumberValue("@.amount").isEqualTo(123.0);
	}

	
	@Test
	void OrderDeserializationTest() throws IOException {
		String expected = """
			{
				"id": 100,
				"amount": 123.00,
				"owner": "Alice"
			}
		""";
		
		// test that parsing is correct
		assertThat(json.parse(expected)).isEqualTo(new BEOrder(100L, 123.00, "Alice"));

		// test that 'Order' attributes are read correctly
		assertThat(json.parseObject(expected).id()).isEqualTo(100);
		assertThat(json.parseObject(expected).amount()).isEqualTo(123.0);
	}
	

	@Test
	void ordersListSerializationTest() throws IOException {
		assertThat(jsonList.write(beOrders)).isStrictlyEqualToJson("orders/order_expected_list.json");
	}
	
	
	@Test
	void ordersListDeserializationTest() throws IOException {
		String expected="""
				[
					{"id":  99, "amount":  123.99, "owner": "Alice"},
					{"id": 100, "amount": 1100.99, "owner": "Alice"},
					{"id": 200, "amount": 1200.99, "owner": "Alice"},
					{"id": 300, "amount": 1300.99, "owner": "Alice"},
					{"id": 400, "amount": 1400.99, "owner": "Alice"}
				]
				""";
		assertThat(jsonList.parse(expected)).isEqualTo(beOrders);
	}
}
