package com.poly.asm.services;

import com.poly.asm.ResourceNotFoundException;
import com.poly.asm.daos.OrderRepository;
import com.poly.asm.daos.ProductVariantRepository;
import com.poly.asm.entitys.Order;
import com.poly.asm.entitys.OrderDetail;
import com.poly.asm.entitys.ProductVariant;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.List;

@Service
public class OrderService {

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private ProductVariantRepository productVariantRepository;

    private static final List<String> VALID_STATUSES = Arrays.asList("PENDING", "SHIPPING", "DELIVERED", "CANCELED");

    @Transactional
    public void updateOrderStatus(Long id, String status) {
        Order order = orderRepository.findByIdWithDetails(id);
        if (order == null) {
            throw new ResourceNotFoundException("Đơn hàng không tồn tại với ID: " + id);
        }

        if (!VALID_STATUSES.contains(status.toUpperCase())) {
            throw new IllegalArgumentException("Trạng thái không hợp lệ: " + status);
        }

        // Kiểm tra trạng thái hợp lệ khi hủy
        if ("CANCELED".equalsIgnoreCase(status) && !"PENDING".equalsIgnoreCase(order.getStatus())) {
            throw new IllegalStateException("Chỉ có thể hủy đơn hàng ở trạng thái PENDING");
        }

        // Nếu hủy đơn hàng, hoàn kho
        if ("CANCELED".equalsIgnoreCase(status)) {
            for (OrderDetail detail : order.getOrderDetails()) {
                ProductVariant variant = detail.getVariant();
                variant.setStock(variant.getStock() + detail.getQuantity());
                productVariantRepository.save(variant);
            }
        }

        order.setStatus(status.toUpperCase());
        orderRepository.save(order);
    }
}