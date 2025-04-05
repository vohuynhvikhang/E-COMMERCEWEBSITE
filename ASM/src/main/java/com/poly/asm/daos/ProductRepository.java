package com.poly.asm.daos;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.poly.asm.entitys.Product;

import java.math.BigDecimal;
import java.util.List;

public interface ProductRepository extends JpaRepository<Product, Long> {
    // Tìm sản phẩm theo tên
    List<Product> findByNameContaining(String name);

    // Lọc sản phẩm theo categoryId
    Page<Product> findByCategoryId(Long categoryId, Pageable pageable);


    // Lọc sản phẩm giảm giá (giả sử bạn có thêm trường `discount` hoặc `price`)
    @Query("SELECT p FROM Product p WHERE p.price <= :discountPrice ORDER BY p.price DESC")
    List<Product> findTop10ByDiscountPrice(BigDecimal discountPrice);

    // Tìm sản phẩm theo tên và loại, có thể thêm điều kiện tìm kiếm giá
    List<Product> findByNameContainingAndCategoryIdAndPriceBetween(String name, Long categoryId, BigDecimal minPrice, BigDecimal maxPrice);
    
    Page<Product> findAll(Pageable pageable);
    
    List<Product> findByCategoryId(Long categoryId);
    
    Page<Product> findByNameContainingIgnoreCase(String name, Pageable pageable);


}
