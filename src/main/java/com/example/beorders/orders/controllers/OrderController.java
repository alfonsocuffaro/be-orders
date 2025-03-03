package com.example.beorders.orders.controllers;

import java.net.URI;
import java.security.Principal;
import java.util.List;

import org.apache.logging.log4j.util.Strings;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.MediaType;
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
@RequestMapping("/v1/orders")
public class OrderController {
	private final OrderRepository orderRepository;
	
	
	private OrderController(OrderRepository anOrderRepository) {
		this.orderRepository = anOrderRepository;
	}


	@GetMapping
	private ResponseEntity<List<Order>> findAll(
			@RequestParam(required = false) String productType,
			Pageable pageable, Principal principal
	) {
		PageRequest pageRequest = PageRequest.of(
						pageable.getPageNumber(),
						pageable.getPageSize(),
						pageable.getSortOr(Sort.by(Sort.Direction.ASC, "amount"))
					);
		
		// TODO implement a better solution for testing ADMIN roles
		// instead of such a naive one
		if (principal.getName().equals("Admin")) {
			
			Page<Order> adminPage = Strings.isBlank(productType) ?
					orderRepository.findAll(pageRequest)
					: orderRepository.findByProductIgnoreCase(productType, pageRequest);

			return ResponseEntity.ok(adminPage.getContent());
		}
		
		String productOwner = principal.getName();
		Page<Order> page = Strings.isBlank(productType) ?
				orderRepository.findByOwner(productOwner, pageRequest)
				: orderRepository.findByOwnerAndProductIgnoreCase(productOwner, productType, pageRequest);
		
		return ResponseEntity.ok(page.getContent());
	}
	
	
	@GetMapping("/{requestedId}")
	private ResponseEntity<Order> findById(@PathVariable Long requestedId, Principal principal) {
		
		Order order = findOrder(requestedId, principal);
		
		return order == null ?
				ResponseEntity.notFound().build()
				: ResponseEntity.ok(order);		
	}
	
	
	@PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
	private ResponseEntity<Void> createOrder(@RequestBody Order newOrder, UriComponentsBuilder ucb, Principal principal) {

		String newOwner = Strings.isEmpty(newOrder.owner()) ? principal.getName() : newOrder.owner();
		Order savedOrderWithOwner = new Order(null, newOrder.amount(), newOwner, newOrder.product(), newOrder.quantity());
		Order savedOrder = orderRepository.save(savedOrderWithOwner);
		URI locationOfSavedOrder = ucb
				.path("/v1/orders/{newOrderId}")
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
		
		String newOwner = Strings.isEmpty(update.owner()) ? principal.getName() : update.owner();
		Order updatedOrder = new Order(order.id(), update.amount(), newOwner, update.product(), update.quantity());
		orderRepository.save(updatedOrder);
		
		return ResponseEntity.noContent().build();
	}
	
	
	@DeleteMapping("/{id}")
	private ResponseEntity<Void> deleteOrder(@PathVariable Long id, Principal principal) {
		
		// TODO implement a better solution instead of such a naive one
		if (principal.getName().equals("Admin")) {
			// check if the order exists in the database
			if (!orderRepository.existsById(id)) {
				return ResponseEntity.notFound().build();
			}
			orderRepository.deleteById(id);
			return ResponseEntity.noContent().build();
		}
		
		// check if the order exists in the database AND the principal owns the record
		if (!orderRepository.existsByIdAndOwner(id, principal.getName())) {
			return ResponseEntity.notFound().build();
		}
		orderRepository.deleteById(id);
		return ResponseEntity.noContent().build();
	}
	
	
	private Order findOrder(Long requestedId, Principal principal) {
		if (principal.getName().equals("Admin")) {
			return orderRepository.findById(requestedId).orElse(null);
		}
		return orderRepository.findByIdAndOwner(requestedId, principal.getName());
	}
		
}
