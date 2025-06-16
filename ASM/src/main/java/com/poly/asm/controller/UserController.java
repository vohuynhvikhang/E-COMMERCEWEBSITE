package com.poly.asm.controller;

import com.poly.asm.daos.UserRepository;
import com.poly.asm.entitys.Cart;
import com.poly.asm.entitys.User;
import com.poly.asm.services.CartService;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class UserController {

	@Autowired
    private CartService cartService;

    
    
    @Autowired
    private UserRepository userRepository;

    @ModelAttribute
    public void addAttributesToAllModels(HttpServletRequest request, Model model) {
        Cart cart = cartService.getCart(request); // Đúng
        int cartItemCount = cart != null ? cart.getCartItems().size() : 0;
        model.addAttribute("cartItemCount", cartItemCount);
    }
    
    @GetMapping("/user/edit")
    public String editProfilePage(HttpSession session, Model model) {
        User user = (User) session.getAttribute("user");
        if (user == null) {
            return "redirect:/login";
        }
        model.addAttribute("user", user);
        return "web/edit-profile";
    }

    @PostMapping("/user/update")
    public String updateProfile(
            @RequestParam String fullname,
            @RequestParam String phone,
            @RequestParam(required = false) String password,
            HttpSession session,
            Model model) {
        User user = (User) session.getAttribute("user");
        if (user == null) {
            return "redirect:/login";
        }

        user.setFullname(fullname);
        user.setPhone(phone);

        if (password != null && !password.isEmpty()) {
            user.setPassword(password); // TODO: Nên mã hóa mật khẩu (BCrypt)
        }

        userRepository.save(user);
        session.setAttribute("user", user);
        model.addAttribute("message", "Cập nhật thành công!");
        return "redirect:/home";
    }
}