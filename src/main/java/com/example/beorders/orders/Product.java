package com.example.beorders.orders;

import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

@Table(name = "BE_PRODUCT")
public record Product(@Id Long id, String name, Double price) {
}
