package com.example.beorders.orders;

import java.net.URI;
import java.util.List;
import java.util.Optional;

import org.apache.logging.log4j.util.Strings;
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
@RequestMapping("/admin/orders")
public class AdminOrderController {
	private final OrderRepository orderRepository;
	
	private AdminOrderController(OrderRepository anOrderRepository) {
		this.orderRepository = anOrderRepository;
	}


	@GetMapping
	private ResponseEntity<List<Order>> findAll(Pageable pageable, Principal principal) {
		
		// TODO implement a better solution instead of such a naive one
		if (principal.getName().equals("Admin")) {
			Page<Order> adminPage = orderRepository.findAll(
				PageRequest.of(
					pageable.getPageNumber(),
					pageable.getPageSize(),
					pageable.getSortOr(Sort.by(Sort.Direction.ASC, "amount"))
				));

			return ResponseEntity.ok(adminPage.getContent());
		}
		
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
		
		Optional<Order> order = findOrder(requestedId, principal);
		
		return order.isEmpty() ?
				ResponseEntity.notFound().build()
				: ResponseEntity.ok(order.get());		
	}
	
	
	@PostMapping
	private ResponseEntity<Void> createOrder(@RequestBody Order newOrder, UriComponentsBuilder ucb, Principal principal) {

		String newOwner = Strings.isEmpty(newOrder.owner()) ? principal.getName() : newOrder.owner();
		Order savedOrderWithOwner = new Order(null, newOrder.amount(), newOwner);
		Order savedOrder = orderRepository.save(savedOrderWithOwner);
		URI locationOfSavedOrder = ucb
				.path("/admin/orders/{newOrderId}")
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
		
		Order updatedOrder = new Order(order.id(), update.amount(), newOwner);
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
	
//	private Order findOrder2(Long requestedId, Principal principal) {
//		return orderRepository.findByIdAndOwner(requestedId, principal.getName());
//	}
		
}
