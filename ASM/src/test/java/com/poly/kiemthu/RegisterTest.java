package com.poly.kiemthu;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.testng.AbstractTestNGSpringContextTests;
import org.springframework.ui.ExtendedModelMap;
import org.springframework.ui.Model;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.poly.asm.AsmApplication;
import com.poly.asm.controller.AuthController;
import com.poly.asm.daos.UserRepository;
import com.poly.asm.entitys.User;

import jakarta.transaction.Transactional;

@SpringBootTest(classes = AsmApplication.class)
public class RegisterTest extends AbstractTestNGSpringContextTests {

    @Autowired
    private AuthController authController;

    @Autowired
    private UserRepository userRepository;

    private Model model;

    @BeforeMethod
    public void setUp() {
        Assert.assertNotNull(authController, "AuthController bị null!");
        Assert.assertNotNull(userRepository, "UserRepository bị null!");
        model = new ExtendedModelMap();
        userRepository.deleteByEmail("existing@gmail.com");
    }

    /**
     * TC-001: Kiểm tra đăng ký với thông tin hợp lệ
     */
    @Test
    @Transactional
    public void testRegister_Success() {
        // Xoá user nếu đã tồn tại
        User user = userRepository.findByUsername("user4");
        if (user != null) {
            userRepository.delete(user);
        }

        user = userRepository.findByEmail("user4@gmail.com");
        if (user != null) {
            userRepository.delete(user);
        }

        // Test đăng ký
        String view = authController.register("user4", "123", "User 4", "user4@gmail.com", "0123456789", "HCMC", model);

        // Kiểm tra kết quả
        Assert.assertEquals(view, "/web/login");
        Assert.assertEquals(model.getAttribute("message"), "Đăng ký thành công! Vui lòng đăng nhập.");
        Assert.assertNotNull(userRepository.findByUsername("newUser"), "User chưa được lưu!");
    }

    /**
     * TC-002: Kiểm tra đăng ký khi nhập Username đã tồn tại
     */
    @Test
    @Transactional
    public void testRegister_UsernameExists() {
        // Thêm user trước
        User existingUser = new User(null, "user4", "123", "user4@gmail.com", "User 4", "0123456789", "HCMC", "USER", true);
        userRepository.save(existingUser);

        // Đăng ký với username đã tồn tại
        String view = authController.register("user4", "123", "User 5", "user4@gmail.com", "0987654321", "Hanoi", model);

        Assert.assertEquals(view, "/web/register");
        Assert.assertEquals(model.getAttribute("message"), "Tên đăng nhập đã tồn tại!");
    }

    /**
     * TC-003: Kiểm tra đăng ký khi nhập Email đã tồn tại
     */
    @Test
    @Transactional
    public void testRegister_EmailExists() {
        // Thêm user trước
        User existingUser = new User(null, "user4", "123", "user4@gmail.com", "User 4", "0123456789", "HCMC", "USER", true);
        userRepository.save(existingUser);

        // Đăng ký với email đã tồn tại
        String view = authController.register("user5", "123", "User 5", "user4@gmail.com", "0987654321", "Hanoi", model);

        Assert.assertEquals(view, "/web/register");
        Assert.assertEquals(model.getAttribute("message"), "Email đã tồn tại!");
    }
}
