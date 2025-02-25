package com.example.beorders.orders;

import java.util.Optional;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


@RestController
@RequestMapping("/orders")
public class OrderController {
	private final OrderRepository orderRepository;
	
	private OrderController(OrderRepository anOrderRepository) {
		this.orderRepository = anOrderRepository;
	}
	
	@GetMapping("/{requestedId}")
	private ResponseEntity<BEOrder> findById(@PathVariable Long requestedId) {
		
		Optional<BEOrder> optionalOrder = orderRepository.findById(requestedId);
		
		if(optionalOrder.isPresent()) {
			return ResponseEntity.ok(optionalOrder.get());
		}
		
		return ResponseEntity.notFound().build();
	}

}
