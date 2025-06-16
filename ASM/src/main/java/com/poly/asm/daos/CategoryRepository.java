package com.poly.asm.daos;

import com.poly.asm.entitys.Category;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CategoryRepository extends JpaRepository<Category, Long> {
    // Lấy tất cả các loại sản phẩm
    List<Category> findAll();

    // Thêm phương thức tìm theo tên (tùy chọn)
    Category findByName(String name);
}