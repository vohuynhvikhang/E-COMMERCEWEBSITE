package com.poly.asm.controller;

import java.util.List;
import java.util.Optional;

import com.poly.asm.entitys.User;
import com.poly.asm.entitys.Order;
import com.poly.asm.entitys.OrderDetail;
import com.poly.asm.daos.OrderRepository;
import com.poly.asm.daos.OrderDetailRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;

@Controller
@RequestMapping("/orders")
public class OrderController {

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private OrderDetailRepository orderDetailRepository;
    

    // Danh sách đơn hàng của user
    @GetMapping
    public String viewOrders(HttpServletRequest request, Model model) {
        User user = (User) request.getSession().getAttribute("user");
        if (user == null) {
            return "redirect:/login";
        }

        List<Order> orders = orderRepository.findByUser_Id(user.getId());
        model.addAttribute("orders", orders);
        return "web/orders"; // danh sách đơn hàng
    }

    @GetMapping("/{orderId}")
    public String viewOrderDetails(@PathVariable Long orderId, HttpServletRequest request, Model model) {
        User user = (User) request.getSession().getAttribute("user");
        if (user == null) {
            return "redirect:/login";
        }

        Optional<Order> orderOpt = orderRepository.findById(orderId);
        if (orderOpt.isPresent()) {
            Order order = orderOpt.get();
            // Kiểm tra đơn hàng có thuộc user không
            if (!order.getUser().getId().equals(user.getId())) {
                return "redirect:/orders";
            }

            List<OrderDetail> details = orderDetailRepository.findByOrder(order);
            model.addAttribute("order", order);
            model.addAttribute("details", details);
            return "web/order-details";
        }

        return "redirect:/orders";
    }

    // Hủy đơn hàng (nếu trạng thái là PENDING)
    @PostMapping("/cancel/{orderId}")
    public String cancelOrder(@PathVariable Long orderId, HttpServletRequest request) {
        User user = (User) request.getSession().getAttribute("user");
        if (user == null) {
            return "redirect:/login";
        }

        Optional<Order> orderOpt = orderRepository.findById(orderId);
        if (orderOpt.isPresent()) {
            Order order = orderOpt.get();
            if (order.getUser().getId().equals(user.getId()) && "PENDING".equals(order.getStatus())) {
                order.setStatus("CANCELED");
                orderRepository.save(order);
            }
        }

        return "redirect:/orders";
    }
}
