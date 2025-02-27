package com.example.beorders.orders;

import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

@Table(name = "BE_ORDER")
public record Order(@Id Long id, Double amount, String owner) {

}
