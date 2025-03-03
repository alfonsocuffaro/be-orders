package com.example.beorders.orders.controllers;

import java.net.URI;
import java.security.Principal;
import java.util.List;
import java.util.Optional;

import org.apache.logging.log4j.util.Strings;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.util.UriComponentsBuilder;

import com.example.beorders.orders.Order;
import com.example.beorders.orders.OrderRepository;


@RestController
@RequestMapping("/v1/admin/orders")
public class AdminOrderController {
	private final OrderRepository orderRepository;
	
	
	private AdminOrderController(OrderRepository anOrderRepository) {
		this.orderRepository = anOrderRepository;
	}


	@GetMapping
	private ResponseEntity<List<Order>> findAll(
			@RequestParam(required = false) String productType,
			Pageable pageable,
			Principal principal
	) {
		PageRequest pageRequest = PageRequest.of(
						pageable.getPageNumber(),
						pageable.getPageSize(),
						pageable.getSortOr(Sort.by(Sort.Direction.ASC, "amount"))
					);
		
		Page<Order> adminPage = Strings.isBlank(productType) ?
				orderRepository.findAll(pageRequest)
				: orderRepository.findByProduct(productType, pageRequest);

		return ResponseEntity.ok(adminPage.getContent());
	}
	
	
	@GetMapping("/{requestedId}")
	private ResponseEntity<Order> findById(@PathVariable Long requestedId, Principal principal) {
		
		Optional<Order> order = findOrder(requestedId, principal);
		
		return order.isEmpty() ?
				ResponseEntity.notFound().build()
				: ResponseEntity.ok(order.get());		
	}
	
	
	@PostMapping
	private ResponseEntity<Void> createOrder(@RequestBody Order newOrder, UriComponentsBuilder ucb, Principal principal) {

		String newOwner = Strings.isEmpty(newOrder.owner()) ? principal.getName() : newOrder.owner();
		Order savedOrderWithOwner = new Order(null, newOrder.amount(), newOwner, newOrder.product(), newOrder.quantity());
		Order savedOrder = orderRepository.save(savedOrderWithOwner);
		URI locationOfSavedOrder = ucb
				.path("/v1/admin/orders/{newOrderId}")
				.buildAndExpand(savedOrder.id())
				.toUri();
		return ResponseEntity.created(locationOfSavedOrder).build();
	}
	
	
	@PutMapping("/{requestedId}")
	private ResponseEntity<Void> putOrder(@PathVariable Long requestedId, @RequestBody Order update, Principal principal) {

		Optional<Order> optionalOrder = findOrder(requestedId, principal);
		
		if (optionalOrder.isEmpty()) {
			return ResponseEntity.notFound().build();
		}
		
		Order order = optionalOrder.get();
		String newOwner = Strings.isEmpty(order.owner()) ? principal.getName() : order.owner();
		
		Order updatedOrder = new Order(order.id(), update.amount(), newOwner, update.product(), update.quantity());
		orderRepository.save(updatedOrder);
		
		return ResponseEntity.noContent().build();
	}
	
	
	@DeleteMapping("/{id}")
	private ResponseEntity<Void> deleteOrder(@PathVariable Long id, Principal principal) {
		// check if the order exists in the database AND the principal own the record
		if (!orderRepository.existsById(id)) {
			return ResponseEntity.notFound().build();
		}
		orderRepository.deleteById(id);
		return ResponseEntity.noContent().build();
	}
	
	private Optional<Order> findOrder(Long requestedId, Principal principal) {
		return orderRepository.findById(requestedId);
	}
		
}
