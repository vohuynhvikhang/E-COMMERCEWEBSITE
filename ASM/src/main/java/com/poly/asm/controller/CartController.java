package com.poly.asm.controller;

import com.poly.asm.daos.OrderDetailRepository;
import com.poly.asm.daos.OrderRepository;
import com.poly.asm.daos.ProductRepository;
import com.poly.asm.entitys.CartItem;
import com.poly.asm.entitys.Order;
import com.poly.asm.entitys.OrderDetail;
import com.poly.asm.entitys.User;
import com.poly.asm.services.CartService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.Map;

@Controller
@RequestMapping("/cart")
public class CartController {

    @Autowired
    private CartService cartService;
    
    @Autowired
    private OrderRepository orderRepository;
    
    @Autowired
    private OrderDetailRepository orderDetailRepository;
    
    @Autowired
    private ProductRepository productRepository;

    @GetMapping
    public String viewCart(HttpServletRequest request, Model model) {
        // Kiểm tra xem người dùng đã đăng nhập chưa
        if (request.getSession().getAttribute("user") == null) {
            return "redirect:/login"; // Nếu chưa đăng nhập, chuyển hướng tới trang đăng nhập
        }

        Map<Long, CartItem> cart = cartService.getCartFromCookie(request);
        double total = cartService.getTotalPrice(cart);

        model.addAttribute("cartItems", cart.values());
        model.addAttribute("cartTotal", total);

        return "web/cart";
    }

    @PostMapping("/add/{productId}")
    public String addToCart(@PathVariable Long productId,
                            HttpServletRequest request,
                            HttpServletResponse response) {
        cartService.addToCart(request, response, productId);
        return "redirect:/cart";
    }

    @PostMapping("/update/{productId}")
    public String updateCart(@PathVariable Long productId,
                             @RequestParam int quantity,
                             HttpServletRequest request,
                             HttpServletResponse response) {
        cartService.updateCart(request, response, productId, quantity);
        return "redirect:/cart";
    }

    @PostMapping("/remove/{productId}")
    public String removeFromCart(@PathVariable Long productId,
                                 HttpServletRequest request,
                                 HttpServletResponse response) {
        cartService.removeFromCart(request, response, productId);
        return "redirect:/cart";
    }
    
    @GetMapping("/checkout")
    public String checkout(HttpServletRequest request, Model model) {
        // Kiểm tra xem người dùng đã đăng nhập chưa
        if (request.getSession().getAttribute("user") == null) {
            return "redirect:/login"; // Nếu chưa đăng nhập, chuyển hướng tới trang đăng nhập
        }

        // Lấy giỏ hàng từ cookie
        Map<Long, CartItem> cart = cartService.getCartFromCookie(request);
        double total = cartService.getTotalPrice(cart);

        // Lấy thông tin người dùng từ session
        User user = (User) request.getSession().getAttribute("user");

        // Truyền thông tin giỏ hàng và người dùng lên view
        model.addAttribute("cartItems", cart.values());
        model.addAttribute("cartTotal", total);
        model.addAttribute("user", user);

        return "web/checkout"; // Trang checkout
    }
    
    @PostMapping("/process-payment")
    public String processPayment(@RequestParam String address,
                                  HttpServletRequest request,
                                  HttpServletResponse response) {
        // Kiểm tra xem người dùng đã đăng nhập chưa
        if (request.getSession().getAttribute("user") == null) {
            return "redirect:/login"; // Nếu chưa đăng nhập, chuyển hướng tới trang đăng nhập
        }

        // Lấy thông tin giỏ hàng
        Map<Long, CartItem> cart = cartService.getCartFromCookie(request);
        double total = cartService.getTotalPrice(cart);

        // Tiến hành xử lý thanh toán (giả sử không có hệ thống thanh toán thực tế ở đây)
        // Bạn có thể lưu đơn hàng vào cơ sở dữ liệu hoặc gửi thông tin qua email, v.v.

        // Xóa giỏ hàng sau khi thanh toán
        cartService.clearCart(request, response);

        // Chuyển hướng người dùng đến trang thành công
        return "cart/success"; 
    }


    @PostMapping("/payment")
    public String payment(@RequestParam String fullname,
                          @RequestParam String phone,
                          @RequestParam String address,
                          @RequestParam String paymentMethod,
                          HttpServletRequest request) {

        // Lấy giỏ hàng từ cookie
        Map<Long, CartItem> cart = cartService.getCartFromCookie(request);
        double total = cartService.getTotalPrice(cart);

        // Lấy thông tin người dùng từ session
        User user = (User) request.getSession().getAttribute("user");

        // Tạo đơn hàng mới
        Order order = new Order();
        order.setFullname(fullname);
        order.setPhone(phone);
        order.setAddress(address);
        order.setPaymentMethod(paymentMethod);
        order.setTotalPrice(total);
        order.setUser(user);
        order.setStatus("PENDING");

        // Lưu đơn hàng vào cơ sở dữ liệu
        order = orderRepository.save(order);

        // Lưu chi tiết đơn hàng
        for (CartItem cartItem : cart.values()) {
            OrderDetail orderDetail = new OrderDetail();
            orderDetail.setOrder(order);
            orderDetail.setProduct(productRepository.findById(cartItem.getProductId()).orElse(null));
            orderDetail.setQuantity(cartItem.getQuantity());
            orderDetail.setPrice(cartItem.getTotalPrice());
            orderDetailRepository.save(orderDetail);
        }

        // Xóa giỏ hàng sau khi thanh toán thành công
        cartService.clearCart(request, null);

        // Chuyển hướng đến trang thành công
        return "cart/success";
    }

}
