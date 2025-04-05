package com.poly.asm.daos;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.poly.asm.entitys.Order;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {

	// Lấy đơn hàng có trạng thái là "PENDING"
	List<Order> findByStatus(String status);

	// Thay đổi query để truy vấn theo user và status
    List<Order> findByUser_IdAndStatus(Long userId, String status);
    
    List<Order> findByUser_Id(Long userId);
}
