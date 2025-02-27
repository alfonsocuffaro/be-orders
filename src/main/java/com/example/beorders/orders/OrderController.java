package com.example.beorders.orders;

import java.net.URI;
import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.util.UriComponentsBuilder;
import java.security.Principal;


@RestController
@RequestMapping("/orders")
public class OrderController {
	private final OrderRepository orderRepository;
	
	private OrderController(OrderRepository anOrderRepository) {
		this.orderRepository = anOrderRepository;
	}


	@GetMapping
	private ResponseEntity<List<Order>> findAll(Pageable pageable, Principal principal) {
		Page<Order> page = orderRepository.findByOwner(
			principal.getName(),
			PageRequest.of(
				pageable.getPageNumber(),
				pageable.getPageSize(),
				pageable.getSortOr(Sort.by(Sort.Direction.ASC, "amount"))
			));
		return ResponseEntity.ok(page.getContent());
	}
	
	@GetMapping("/{requestedId}")
	private ResponseEntity<Order> findById(@PathVariable Long requestedId, Principal principal) {
		
		Order order = findOrder(requestedId, principal);
		
		return order == null ?
				ResponseEntity.notFound().build()
				: ResponseEntity.ok(order);		
	}
	
	
	@PostMapping
	private ResponseEntity<Void> createOrder(@RequestBody Order newOrder, UriComponentsBuilder ucb, Principal principal) {

		Order savedOrderWithOwner = new Order(null,newOrder.amount(), principal.getName());
		Order savedOrder = orderRepository.save(savedOrderWithOwner);
		URI locationOfSavedOrder = ucb
				.path("/orders/{newOrderId}")
				.buildAndExpand(savedOrder.id())
				.toUri();
		return ResponseEntity.created(locationOfSavedOrder).build();
	}
	
	
	@PutMapping("/{requestedId}")
	private ResponseEntity<Void> putOrder(@PathVariable Long requestedId, @RequestBody Order update, Principal principal) {

		Order order = findOrder(requestedId, principal);
		
		if (order == null) {
			return ResponseEntity.notFound().build();
		}
		
		Order updatedOrder = new Order(order.id(), update.amount(), principal.getName());
		orderRepository.save(updatedOrder);
		
		return ResponseEntity.noContent().build();
	}
	
	
	@DeleteMapping("/{id}")
	private ResponseEntity<Void> deleteCashCard(@PathVariable Long id, Principal principal) {
		// check if the order exists in the database AND the principal own the record
		if (!orderRepository.existsByIdAndOwner(id, principal.getName())) {
			return ResponseEntity.notFound().build();
		}
		orderRepository.deleteById(id);
		return ResponseEntity.noContent().build();
	}
	
	private Order findOrder(Long requestedId, Principal principal) {
		return orderRepository.findByIdAndOwner(requestedId, principal.getName());
	}
		
}
