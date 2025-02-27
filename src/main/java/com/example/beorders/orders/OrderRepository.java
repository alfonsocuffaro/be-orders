package com.example.beorders.orders;

import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.PagingAndSortingRepository;

interface OrderRepository extends CrudRepository<BEOrder, Long>, PagingAndSortingRepository<BEOrder, Long> {

}
