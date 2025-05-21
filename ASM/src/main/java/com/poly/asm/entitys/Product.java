package com.poly.asm.entitys;

import java.math.BigDecimal;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Data
@Table(name = "Products")
public class Product {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(length = 500)
    private String description;

    @Column(nullable = false, precision = 18, scale = 2)
    private BigDecimal price; 

    @Column(nullable = false)
    private int stock;

    private String image;

    @ManyToOne
    @JoinColumn(name = "category_id")
    private Category category;
}

