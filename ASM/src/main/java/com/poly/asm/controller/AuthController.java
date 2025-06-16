package com.poly.asm.controller;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.CookieValue;
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
    public String loginPage(@CookieValue(name = "rememberedUsername", required = false) String rememberedUsername,
                           @RequestParam(required = false) String error, Model model) {
        // Tự động điền username từ cookie nếu có
        if (rememberedUsername != null) {
            model.addAttribute("username", rememberedUsername);
        }
        if (error != null) {
            model.addAttribute("message", error);
        }
        return "web/login";
    }

    @PostMapping("/login")
    public String login(@RequestParam String username, @RequestParam String password,
                       @RequestParam(required = false) String remember, HttpSession session,
                       HttpServletResponse response, Model model) {
        User user = userRepository.findByUsername(username);

        if (user != null && user.getPassword().equals(password) && user.getActive()) {
            // Lưu user vào session
            session.setAttribute("user", user);

            // Xử lý "Ghi nhớ tài khoản"
            if (remember != null) { // Nếu checkbox được chọn
                Cookie usernameCookie = new Cookie("rememberedUsername", username);
                usernameCookie.setMaxAge(30 * 24 * 60 * 60); // 30 ngày
                usernameCookie.setPath("/");
                response.addCookie(usernameCookie);
            } else {
                // Xóa cookie nếu không chọn "remember"
                Cookie usernameCookie = new Cookie("rememberedUsername", null);
                usernameCookie.setMaxAge(0);
                usernameCookie.setPath("/");
                response.addCookie(usernameCookie);
            }

            // Lưu userId vào cookie (như hiện tại)
            Cookie userIdCookie = new Cookie("userId", user.getId().toString());
            userIdCookie.setMaxAge(60 * 60 * 24 * 7); // 1 tuần
            userIdCookie.setPath("/");
            response.addCookie(userIdCookie);

            // Chuyển hướng dựa trên role
            return user.getRole().equalsIgnoreCase("ADMIN") ? "redirect:/admin/index" : "redirect:/home";
        } else {
            model.addAttribute("message", "Sai tài khoản, mật khẩu hoặc tài khoản bị khóa!");
            return "web/login";
        }
    }

    @GetMapping("/logout")
    public String logout(HttpSession session, HttpServletRequest request, HttpServletResponse response) {
        // Xóa session
        session.invalidate();

        // Tìm và xóa cookie userId
        if (request.getCookies() != null) {
            for (Cookie cookie : request.getCookies()) {
                if ("userId".equals(cookie.getName()) || "rememberedUsername".equals(cookie.getName())) {
                    cookie.setMaxAge(0);
                    cookie.setPath("/");
                    response.addCookie(cookie);
                }
            }
        }

        return "redirect:/home";
    }

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