package com.example.beorders.orders;

import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.PagingAndSortingRepository;

interface OrderRepository extends CrudRepository<Order, Long>, PagingAndSortingRepository<Order, Long> {

	Optional<Order> findById(Long id);
	Order           findByIdAndOwner(Long id, String owner);
	boolean         existsByIdAndOwner(Long id, String owner);

	Page<Order>     findByOwner(String owner, PageRequest pageRequest);
	Page<Order>     findByProduct(String productType, Pageable pageRequest);
	Page<Order>     findByOwnerAndProduct(String owner, String productType, Pageable pageRequest);
	Page<Order>     findAll(Pageable pageRequest);
}
