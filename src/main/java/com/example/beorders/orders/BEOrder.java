package com.example.beorders.orders;

import org.springframework.data.annotation.Id;

public record BEOrder(@Id Long id, Double amount) {

}
