package com.poly.asm.controller;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

import com.poly.asm.entitys.CartItem;
import com.poly.asm.services.CartService;

import jakarta.servlet.http.HttpServletRequest;

@ControllerAdvice
public class GlobalController {

    @Autowired
    private CartService cartService;

    @ModelAttribute
    public void globalAttributes(HttpServletRequest request, Model model) {
        Map<Long, CartItem> cart = cartService.getCartFromCookie(request);
        int cartItemCount = cart.values().stream().mapToInt(CartItem::getQuantity).sum();
        model.addAttribute("cartItemCount", cartItemCount);
    }
}

