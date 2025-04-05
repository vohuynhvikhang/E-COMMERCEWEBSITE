package com.poly.asm.services;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.poly.asm.daos.ProductRepository;
import com.poly.asm.entitys.CartItem;
import com.poly.asm.entitys.Product;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Service
public class CartService {

    @Autowired
    private ProductRepository productRepository;

    private final ObjectMapper objectMapper = new ObjectMapper();

    // Lấy tên người dùng từ session, mặc định là "guest"
    private String getUsernameFromSession(HttpServletRequest request) {
        Object user = request.getSession().getAttribute("user");
        if (user != null) {
            try {
                return (String) user.getClass().getMethod("getUsername").invoke(user);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return "guest";
    }

    // Tạo tên cookie dựa trên tên người dùng
    private String getCartCookieName(HttpServletRequest request) {
        return "cart_" + getUsernameFromSession(request);
    }

    // Thêm sản phẩm vào giỏ hàng
    public void addToCart(HttpServletRequest request, HttpServletResponse response, Long productId) {
        Map<Long, CartItem> cart = getCartFromCookie(request);

        Optional<Product> optionalProduct = productRepository.findById(productId);
        if (optionalProduct.isPresent()) {
            Product product = optionalProduct.get();
            cart.compute(productId, (id, item) -> {
                if (item == null) {
                    return new CartItem(null, product.getId(), product.getName(),
                            product.getPrice().doubleValue(), 1, product.getImage(), null);
                } else {
                    item.setQuantity(item.getQuantity() + 1);
                    return item;
                }
            });

            saveCartToCookie(request, response, cart);
        }
    }

    // Cập nhật số lượng sản phẩm trong giỏ hàng
    public void updateCart(HttpServletRequest request, HttpServletResponse response, Long productId, int quantity) {
        Map<Long, CartItem> cart = getCartFromCookie(request);
        if (cart.containsKey(productId)) {
            if (quantity > 0) {
                cart.get(productId).setQuantity(quantity);
            } else {
                cart.remove(productId);
            }
            saveCartToCookie(request, response, cart);
        }
    }

    // Xóa sản phẩm khỏi giỏ hàng
    public void removeFromCart(HttpServletRequest request, HttpServletResponse response, Long productId) {
        Map<Long, CartItem> cart = getCartFromCookie(request);
        cart.remove(productId);
        saveCartToCookie(request, response, cart);
    }

    // Lấy giỏ hàng từ cookie
    public Map<Long, CartItem> getCartFromCookie(HttpServletRequest request) {
        Map<Long, CartItem> cart = new HashMap<>();
        String cartCookieName = getCartCookieName(request);

        try {
            if (request.getCookies() != null) {
                for (Cookie cookie : request.getCookies()) {
                    if (cartCookieName.equals(cookie.getName())) {
                        String json = URLDecoder.decode(cookie.getValue(), "UTF-8");
                        CartItem[] items = objectMapper.readValue(json, CartItem[].class);
                        for (CartItem item : items) {
                            cart.put(item.getProductId(), item);
                        }
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return cart;
    }

    // Lưu giỏ hàng vào cookie
    private void saveCartToCookie(HttpServletRequest request, HttpServletResponse response, Map<Long, CartItem> cart) {
        try {
            String json = objectMapper.writeValueAsString(cart.values());
            String encoded = URLEncoder.encode(json, "UTF-8");

            String cartCookieName = getCartCookieName(request);
            Cookie cookie = new Cookie(cartCookieName, encoded);
            cookie.setMaxAge(7 * 24 * 60 * 60); // Giới hạn thời gian sống cookie 7 ngày
            cookie.setPath("/"); // Đảm bảo cookie có thể truy cập từ mọi URL
            response.addCookie(cookie);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // Tính tổng giá trị của giỏ hàng
    public double getTotalPrice(Map<Long, CartItem> cart) {
        return cart.values().stream()
                .mapToDouble(item -> item.getTotalPrice())
                .sum();
    }

    public void clearCart(HttpServletRequest request, HttpServletResponse response) {
        String cartCookieName = getCartCookieName(request);
        Cookie cookie = new Cookie(cartCookieName, "");
        cookie.setMaxAge(0); // Xóa cookie
        cookie.setPath("/");

        // Kiểm tra xem response có phải là null không trước khi thêm cookie
        if (response != null) {
            response.addCookie(cookie);
        }
    }


}
