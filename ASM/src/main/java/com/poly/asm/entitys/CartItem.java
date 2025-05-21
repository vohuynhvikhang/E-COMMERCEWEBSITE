package com.poly.asm.entitys;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@NoArgsConstructor
@Table(name = "CartItems")
@JsonIgnoreProperties(ignoreUnknown = true)
public class CartItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long productId;
    private String productName;
    private Double price;
    private Integer quantity;
    private String imageUrl;

    // Constructor
    public CartItem(Long id, Long productId, String productName, Double price, Integer quantity, String imageUrl, Order order) {
        this.id = id;
        this.productId = productId;
        this.productName = productName;
        this.price = price;
        this.quantity = quantity;
        this.imageUrl = imageUrl;
        this.order = order;
    }

    // Mối quan hệ với bảng Order
    @ManyToOne
    @JoinColumn(name = "order_id", nullable = false) 
    private Order order; // Mỗi CartItem thuộc về một Order

    // Tính giá trị tổng của một CartItem (price * quantity)
    public double getTotalPrice() {
        return (this.price != null ? this.price : 0) * (this.quantity != null ? this.quantity : 0);
    }
}
