package com.example.beorders;


import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.json.JsonTest;
import org.springframework.boot.test.json.JacksonTester;
import org.springframework.boot.test.json.JsonContent;
import com.example.beorders.orders.BEOrder;


@JsonTest
public class OrderJsonTest {
	@Autowired
	private JacksonTester<BEOrder> json;
	
	
	@Test
	void OrderSerializationTest() throws IOException {
		BEOrder anOrder = new BEOrder(100L, 123.00);
		JsonContent<BEOrder> jsonOrder = json.write(anOrder);
		
		// test write is correct
		assertThat(jsonOrder).isStrictlyEqualToJson("orders/order_expected.json");
		
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
				"amount": 123.00
			}
		""";
		
		// test that parsing is correct
		assertThat(json.parse(expected)).isEqualTo(new BEOrder(100L, 123.00));

		// test that 'Order' attributes are read correctly
		assertThat(json.parseObject(expected).id()).isEqualTo(100);
		assertThat(json.parseObject(expected).amount()).isEqualTo(123.0);
	}
	
}
