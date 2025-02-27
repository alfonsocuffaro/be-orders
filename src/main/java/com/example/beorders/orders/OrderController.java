package com.example.beorders.orders;

import java.net.URI;
import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
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


	@GetMapping
	private ResponseEntity<List<BEOrder>> findAll(Pageable pageable) {
		Page<BEOrder> page = orderRepository.findAll(
			PageRequest.of(
				pageable.getPageNumber(),
				pageable.getPageSize(),
				pageable.getSortOr(Sort.by(Sort.Direction.ASC, "amount"))
			));
		return ResponseEntity.ok(page.getContent());
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
	
	
	@PutMapping("/{requestedId}")
	private ResponseEntity<Void> putOrder(@PathVariable Long requestedId, @RequestBody BEOrder update) {

		Optional<BEOrder> order = orderRepository.findById(requestedId);
		BEOrder updatedOrder = new BEOrder(order.get().id(), update.amount(), "UNO_USER_FITTIZIO");
		orderRepository.save(updatedOrder);
		
		return ResponseEntity.noContent().build();
	}
		
}
