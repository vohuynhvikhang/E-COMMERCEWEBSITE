package com.poly.asm.controller;

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
public class UserController {

    @Autowired
    private UserRepository userRepository;

    // Hiển thị trang chỉnh sửa thông tin
    @GetMapping("/user/edit")
    public String editProfilePage(HttpSession session, Model model) {
        User user = (User) session.getAttribute("user");
        if (user == null) {
            return "redirect:/login";
        }
        model.addAttribute("user", user);
        return "web/edit-profile";
    }

    // Xử lý cập nhật thông tin
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

        // Cập nhật thông tin
        user.setFullname(fullname);
        user.setPhone(phone);

        if (password != null && !password.isEmpty()) {
            user.setPassword(password);  // Nên hash password ở đây nếu cần
        }

        userRepository.save(user);
        session.setAttribute("user", user); // Cập nhật lại session
        model.addAttribute("message", "Cập nhật thành công!");
        return "redirect:/home";
    }
}
