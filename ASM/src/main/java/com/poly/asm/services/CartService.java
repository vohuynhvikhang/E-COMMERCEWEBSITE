package com.poly.asm.services;

import com.poly.asm.daos.CartRepository;
import com.poly.asm.daos.ProductVariantRepository;
import com.poly.asm.entitys.Cart;
import com.poly.asm.entitys.CartItem;
import com.poly.asm.entitys.ProductVariant;
import com.poly.asm.entitys.User;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Service
public class CartService {

    @Autowired
    private CartRepository cartRepository;

    @Autowired
    private ProductVariantRepository productVariantRepository;

    // Lấy giỏ hàng của user hiện tại
    public Cart getCart(HttpServletRequest request) {
        User user = (User) request.getSession().getAttribute("user");
        if (user == null) {
            return null; // Guest không có giỏ hàng cố định, có thể xử lý riêng nếu cần
        }
        return cartRepository.findByUserId(user.getId())
                .orElseGet(() -> {
                    Cart cart = new Cart(user);
                    return cartRepository.save(cart);
                });
    }

    // Thêm sản phẩm vào giỏ hàng
    public void addToCart(HttpServletRequest request, HttpServletResponse response, Long variantId, int quantity) {
        User user = (User) request.getSession().getAttribute("user");
        if (user == null) {
            return; // Không xử lý guest, có thể thêm logic riêng nếu cần
        }

        Cart cart = getCart(request);
        Optional<ProductVariant> optionalVariant = productVariantRepository.findById(variantId);
        if (optionalVariant.isPresent()) {
            ProductVariant variant = optionalVariant.get();
            if (variant.getStock() < quantity) {
                return;
            }

            // Tìm CartItem hiện có
            CartItem existingItem = cart.getCartItems().stream()
                    .filter(item -> item.getVariant().getId().equals(variantId))
                    .findFirst()
                    .orElse(null);

            if (existingItem != null) {
                int newQuantity = existingItem.getQuantity() + quantity;
                if (newQuantity <= variant.getStock()) {
                    existingItem.setQuantity(newQuantity);
                }
            } else {
                CartItem newItem = new CartItem(cart, variant, variant.getProduct().getName(), variant.getPrice(), quantity);
                cart.getCartItems().add(newItem);
            }
            cartRepository.save(cart);
        }
    }

    // Cập nhật số lượng
    public void updateCart(HttpServletRequest request, Long variantId, int quantity) {
        User user = (User) request.getSession().getAttribute("user");
        if (user == null) {
            return;
        }

        Cart cart = getCart(request);
        cart.getCartItems().stream()
                .filter(item -> item.getVariant().getId().equals(variantId))
                .findFirst()
                .ifPresent(item -> {
                    ProductVariant variant = item.getVariant();
                    if (quantity <= variant.getStock() && quantity > 0) {
                        item.setQuantity(quantity);
                    } else if (quantity <= 0) {
                        cart.getCartItems().remove(item);
                    }
                    cartRepository.save(cart);
                });
    }

    // Xóa sản phẩm
    public void removeFromCart(HttpServletRequest request, Long variantId) {
        User user = (User) request.getSession().getAttribute("user");
        if (user == null) {
            return;
        }

        Cart cart = getCart(request);
        cart.getCartItems().removeIf(item -> item.getVariant().getId().equals(variantId));
        cartRepository.save(cart);
    }

    // Tính tổng giá trị
    public double getTotalPrice(Cart cart) {
        return cart.getCartItems().stream()
                .mapToDouble(item -> item.getTotalPrice().doubleValue())
                .sum();
    }

    // Xóa toàn bộ giỏ hàng (nếu cần)
    public void clearCart(HttpServletRequest request) {
        User user = (User) request.getSession().getAttribute("user");
        if (user != null) {
            Cart cart = getCart(request);
            if (cart != null) {
                cart.getCartItems().clear();
                cartRepository.save(cart);
            }
        }
    }
}