package com.poly.asm.controller;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.poly.asm.daos.UserRepository;
import com.poly.asm.entitys.User;

@Controller
public class AuthController {

    @Autowired
    private UserRepository userRepository;

    @GetMapping("/login")
    public String loginPage(@RequestParam(required = false) String error, Model model) {
        if (error != null) {
            model.addAttribute("message", error);
        }
        return "web/login";
    }

    @PostMapping("/login")
    public String login(@RequestParam String username, @RequestParam String password,
                        HttpSession session, HttpServletResponse response, Model model) {
        User user = userRepository.findByUsername(username);

        if (user != null && user.getPassword().equals(password)) {
            // Lưu userId vào cookie
            Cookie userCookie = new Cookie("userId", user.getId().toString());
            userCookie.setMaxAge(60 * 60 * 24 * 7); // 1 tuần
            userCookie.setPath("/");
            response.addCookie(userCookie);

            // Cài session nếu bạn cần, không dùng cho giỏ hàng
            session.setAttribute("user", user);

            return user.getRole().equalsIgnoreCase("ADMIN") ? "redirect:/admin/index" : "redirect:/home";
        } else {
            model.addAttribute("message", "Sai tài khoản hoặc mật khẩu!");
            return "web/login";
        }
    }

    @GetMapping("/logout")
    public String logout(HttpSession session, HttpServletRequest request, HttpServletResponse response) {
        // Xóa session
        session.invalidate();

        // Tìm userId từ cookie
        String userId = null;
        if (request.getCookies() != null) {
            for (Cookie cookie : request.getCookies()) {
                if ("userId".equals(cookie.getName())) {
                    userId = cookie.getValue();
                }
            }
        }

        // Xóa cookie userId
        Cookie userIdCookie = new Cookie("userId", null);
        userIdCookie.setMaxAge(0);
        userIdCookie.setPath("/");
        response.addCookie(userIdCookie);

        // Xóa cookie giỏ hàng của người dùng (cart_userId)
        if (userId != null) {
            Cookie cartCookie = new Cookie("cart_" + userId, null);
            cartCookie.setMaxAge(0);
            cartCookie.setPath("/");
            response.addCookie(cartCookie);
        }

        return "redirect:/home";
    }

    // -------------------- ĐĂNG KÝ --------------------

    @GetMapping("/register")
    public String registerPage() {
        return "web/register";
    }

    @PostMapping("/register")
    public String register(@RequestParam String username,
                           @RequestParam String password,
                           @RequestParam String fullName,
                           @RequestParam String email,
                           @RequestParam String phone,
                           @RequestParam String address,
                           Model model) {

        if (userRepository.findByUsername(username) != null) {
            model.addAttribute("message", "Tên đăng nhập đã tồn tại!");
            return "web/register";
        }

        if (userRepository.findByEmail(email) != null) {
            model.addAttribute("message", "Email đã tồn tại!");
            return "web/register";
        }

        User newUser = new User();
        newUser.setUsername(username);
        newUser.setPassword(password);
        newUser.setFullname(fullName);
        newUser.setEmail(email);
        newUser.setPhone(phone);
        newUser.setAddress(address);
        newUser.setRole("USER");
        newUser.setActive(true);

        userRepository.save(newUser);

        model.addAttribute("message", "Đăng ký thành công! Vui lòng đăng nhập.");
        return "web/login";
    }
}
