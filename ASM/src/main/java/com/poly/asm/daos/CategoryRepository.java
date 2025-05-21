package com.poly.asm.daos;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.poly.asm.entitys.Category;

public interface CategoryRepository extends JpaRepository<Category, Long> {
	// Lấy tất cả các loại sản phẩm
    List<Category> findAll();
}
