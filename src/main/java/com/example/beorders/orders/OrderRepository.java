package com.example.beorders.orders;

import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.PagingAndSortingRepository;

import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

interface OrderRepository extends CrudRepository<Order, Long>, PagingAndSortingRepository<Order, Long> {

	Optional<Order> findById(Long id);
	Order           findByIdAndOwner(Long id, String owner);
	Page<Order>     findByOwner(String owner, PageRequest pageRequest);
	boolean         existsByIdAndOwner(Long id, String owner);
	
	Page<Order>    findAll(Pageable pageRequest);
	
}
