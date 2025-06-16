package com.poly.asm.controller;

import com.poly.asm.daos.OrderDetailRepository;
import com.poly.asm.daos.OrderRepository;
import com.poly.asm.daos.ProductImageRepository;
import com.poly.asm.daos.ProductVariantRepository;
import com.poly.asm.entitys.Cart;
import com.poly.asm.entitys.CartItem;
import com.poly.asm.entitys.Order;
import com.poly.asm.entitys.OrderDetail;
import com.poly.asm.entitys.ProductVariant;
import com.poly.asm.entitys.User;
import com.poly.asm.services.CartService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
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
    private ProductVariantRepository productVariantRepository;

    @Autowired
    private ProductImageRepository productImageRepository;

    @ModelAttribute
    public void addAttributesToAllModels(HttpServletRequest request, Model model) {
        Cart cart = cartService.getCart(request);
        int cartItemCount = cart != null ? cart.getCartItems().size() : 0;
        model.addAttribute("cartItemCount", cartItemCount);
    }

    @GetMapping
    public String viewCart(HttpServletRequest request, Model model) {
        if (request.getSession().getAttribute("user") == null) {
            return "redirect:/login";
        }

        Cart cart = cartService.getCart(request);
        double total = cartService.getTotalPrice(cart);

        Map<Long, String> imageUrls = new HashMap<>();
        Map<Long, String> sizes = new HashMap<>();
        Map<Long, String> colors = new HashMap<>();
        if (cart != null) {
            for (CartItem item : cart.getCartItems()) {
                Long variantId = item.getVariant().getId();
                String imageUrl = productImageRepository.findPrimaryImageByProductId(item.getVariant().getProduct().getId())
                        .map(img -> img.getImageUrl())
                        .orElse("path/to/placeholder.jpg");
                imageUrls.put(variantId, imageUrl);
                sizes.put(variantId, item.getSize());
                colors.put(variantId, item.getColor());
            }
        }

        List<CartItem> cartItems = cart != null ? cart.getCartItems() : new ArrayList<>();
        model.addAttribute("cartItems", cartItems);
        model.addAttribute("imageUrls", imageUrls);
        model.addAttribute("sizes", sizes);
        model.addAttribute("colors", colors);
        model.addAttribute("cartTotal", total);

        return "web/cart";
    }

    @PostMapping("/add/{variantId}")
    public String addToCart(@PathVariable Long variantId,
                            HttpServletRequest request,
                            HttpServletResponse response) {
        if (request.getSession().getAttribute("user") == null) {
            return "redirect:/login";
        }
        productVariantRepository.findById(variantId).ifPresent(variant -> {
            if (variant.getStock() > 0) {
                cartService.addToCart(request, response, variantId, 1);
            }
        });
        return "redirect:/cart";
    }

    @PostMapping("/add")
    public String addToCartFromForm(
            @RequestParam("productId") Long productId,
            @RequestParam("variantId") Long variantId,
            @RequestParam("quantity") int quantity,
            HttpServletRequest request,
            HttpServletResponse response,
            Model model) {

        User user = (User) request.getSession().getAttribute("user");
        if (user == null) {
            model.addAttribute("error", "Vui lòng đăng nhập để thêm sản phẩm vào giỏ hàng!");
            return "redirect:/login";
        }

        productVariantRepository.findById(variantId).ifPresent(variant -> {
            if (variant.getStock() >= quantity && quantity > 0) {
                cartService.addToCart(request, response, variantId, quantity);
                model.addAttribute("success", "Thêm vào giỏ hàng thành công!");
            } else {
                model.addAttribute("error", "Số lượng vượt quá tồn kho hoặc không hợp lệ!");
            }
        });

        return "redirect:/product/detail/" + productId;
    }

    @PostMapping("/update/{variantId}")
    public String updateCart(@PathVariable Long variantId,
                             @RequestParam int quantity,
                             HttpServletRequest request,
                             HttpServletResponse response) {
        if (request.getSession().getAttribute("user") == null) {
            return "redirect:/login";
        }
        cartService.updateCart(request, variantId, quantity);
        return "redirect:/cart";
    }

    @PostMapping("/remove/{variantId}")
    public String removeFromCart(@PathVariable Long variantId,
                                 HttpServletRequest request,
                                 HttpServletResponse response) {
        if (request.getSession().getAttribute("user") == null) {
            return "redirect:/login";
        }
        cartService.removeFromCart(request, variantId);
        return "redirect:/cart";
    }

    @GetMapping("/checkout")
    public String checkout(HttpServletRequest request, Model model) {
        if (request.getSession().getAttribute("user") == null) {
            return "redirect:/login";
        }

        Cart cart = cartService.getCart(request);
        if (cart == null || cart.getCartItems().isEmpty()) {
            model.addAttribute("error", "Giỏ hàng của bạn trống!");
            return "redirect:/cart";
        }

        double total = cartService.getTotalPrice(cart);
        User user = (User) request.getSession().getAttribute("user");

        model.addAttribute("cartItems", cart.getCartItems());
        model.addAttribute("cartTotal", total);
        model.addAttribute("user", user);

        return "web/checkout";
    }

    @PostMapping("/process-payment")
    public String processPayment(@RequestParam String address,
                                @RequestParam String fullname,
                                @RequestParam String phone,
                                @RequestParam String paymentMethod,
                                HttpServletRequest request,
                                HttpServletResponse response,
                                Model model) {
        if (request.getSession().getAttribute("user") == null) {
            return "redirect:/login";
        }

        Cart cart = cartService.getCart(request);
        if (cart == null || cart.getCartItems().isEmpty()) {
            model.addAttribute("error", "Giỏ hàng trống, không thể thanh toán!");
            return "redirect:/cart";
        }

        double total = cartService.getTotalPrice(cart);
        User user = (User) request.getSession().getAttribute("user");

        Order order = new Order();
        order.setFullname(fullname);
        order.setPhone(phone);
        order.setAddress(address);
        order.setPaymentMethod(paymentMethod);
        order.setTotalPrice(total);
        order.setUser(user);
        order.setStatus("PENDING");

        try {
            order = orderRepository.save(order);

            for (CartItem cartItem : cart.getCartItems()) {
                OrderDetail orderDetail = new OrderDetail();
                orderDetail.setOrder(order);
                orderDetail.setVariant(cartItem.getVariant());
                orderDetail.setQuantity(cartItem.getQuantity());
                orderDetail.setPrice(cartItem.getPrice());
                orderDetailRepository.save(orderDetail);

                // Cập nhật tồn kho
                ProductVariant variant = cartItem.getVariant();
                variant.setStock(variant.getStock() - cartItem.getQuantity());
                productVariantRepository.save(variant);
            }

            // Xóa giỏ hàng sau khi đặt hàng thành công
            cartService.clearCart(request);
            return "redirect:/cart/success";
        } catch (Exception e) {
            model.addAttribute("error", "Đã xảy ra lỗi khi xử lý đơn hàng. Vui lòng thử lại!");
            return "redirect:/cart/checkout";
        }
    }

    @GetMapping("/success")
    public String success(HttpServletRequest request, Model model) {
        if (request.getSession().getAttribute("user") == null) {
            return "redirect:/login";
        }
        return "cart/success";
    }
}