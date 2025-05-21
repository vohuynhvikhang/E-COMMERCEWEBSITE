package com.poly.asm.daos;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.poly.asm.entitys.Order;
import com.poly.asm.entitys.OrderDetail;

public interface OrderDetailRepository extends JpaRepository<OrderDetail, Long> {
	// Lấy danh sách chi tiết theo Order
    List<OrderDetail> findByOrder(Order order);
}
