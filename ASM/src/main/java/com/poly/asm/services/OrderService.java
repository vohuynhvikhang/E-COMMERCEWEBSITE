package com.poly.asm.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.poly.asm.ResourceNotFoundException;
import com.poly.asm.daos.OrderRepository;
import com.poly.asm.entitys.Order;

@Service
public class OrderService {

    @Autowired
    private OrderRepository orderRepository;

    // Cập nhật trạng thái đơn hàng
    public void updateOrderStatus(Long id, String status) {
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Đơn hàng không tồn tại"));
        
        order.setStatus(status);
        orderRepository.save(order);
    }
}