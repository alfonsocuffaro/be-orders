package com.example.beorders.orders;

import java.net.URI;
import java.util.Optional;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.util.UriComponentsBuilder;


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
	
	@PostMapping
	private ResponseEntity<Void> createOrder(@RequestBody BEOrder newOrder, UriComponentsBuilder ucb) {
		
		BEOrder savedOrder = orderRepository.save(newOrder);
		URI locationOfSavedOrder = ucb
				.path("/orders/{newOrderId}")
				.buildAndExpand(savedOrder.id())
				.toUri();
		return ResponseEntity.created(locationOfSavedOrder).build();
	}

}
