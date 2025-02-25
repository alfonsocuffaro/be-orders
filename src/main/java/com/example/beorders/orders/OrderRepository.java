package com.example.beorders.orders;

import org.springframework.data.repository.CrudRepository;

interface OrderRepository extends CrudRepository<BEOrder, Long> {

}
