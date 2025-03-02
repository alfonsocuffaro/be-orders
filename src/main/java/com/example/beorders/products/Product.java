package com.example.beorders.products;

import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

@Table(name = "BE_PRODUCT")
public record Product(@Id Long id, String name, Double price) {

}

