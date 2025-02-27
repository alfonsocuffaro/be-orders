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
import java.security.Principal;


@RestController
@RequestMapping("/orders")
public class OrderController {
	private final OrderRepository orderRepository;
	
	private OrderController(OrderRepository anOrderRepository) {
		this.orderRepository = anOrderRepository;
	}


	@GetMapping
	private ResponseEntity<List<BEOrder>> findAll(Pageable pageable, Principal principal) {
		Page<BEOrder> page = orderRepository.findByOwner(
			principal.getName(),
			PageRequest.of(
				pageable.getPageNumber(),
				pageable.getPageSize(),
				pageable.getSortOr(Sort.by(Sort.Direction.ASC, "amount"))
			));
		return ResponseEntity.ok(page.getContent());
	}
	
	@GetMapping("/{requestedId}")
	private ResponseEntity<BEOrder> findById(@PathVariable Long requestedId, Principal principal) {
		
		BEOrder order = findOrder(requestedId, principal);
		
		return order == null ?
				ResponseEntity.notFound().build()
				: ResponseEntity.ok(order);		
	}
	
	
	@PostMapping
	private ResponseEntity<Void> createOrder(@RequestBody BEOrder newOrder, UriComponentsBuilder ucb, Principal principal) {

		BEOrder savedOrderWithOwner = new BEOrder(null,newOrder.amount(), principal.getName());
		BEOrder savedOrder = orderRepository.save(savedOrderWithOwner);
		URI locationOfSavedOrder = ucb
				.path("/orders/{newOrderId}")
				.buildAndExpand(savedOrder.id())
				.toUri();
		return ResponseEntity.created(locationOfSavedOrder).build();
	}
	
	
	@PutMapping("/{requestedId}")
	private ResponseEntity<Void> putOrder(@PathVariable Long requestedId, @RequestBody BEOrder update, Principal principal) {

		BEOrder order = findOrder(requestedId, principal);
		
		if (order == null) {
			return ResponseEntity.notFound().build();
		}
		
		BEOrder updatedOrder = new BEOrder(order.id(), update.amount(), principal.getName());
		orderRepository.save(updatedOrder);
		
		return ResponseEntity.noContent().build();
	}
	
	
	private BEOrder findOrder(Long requestedId, Principal principal) {
		return orderRepository.findByIdAndOwner(requestedId, principal.getName());
	}
		
}
