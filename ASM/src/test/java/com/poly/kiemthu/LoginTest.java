package com.poly.kiemthu;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockHttpSession;
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

import jakarta.servlet.http.HttpSession;
import jakarta.transaction.Transactional;

@SpringBootTest(classes = AsmApplication.class)
public class LoginTest extends AbstractTestNGSpringContextTests {

    @Autowired
    private AuthController authController;

    @Autowired
    private UserRepository userRepository;

    private Model model;
    private HttpSession session;

    @BeforeMethod
    public void setUp() {
        // Kiểm tra các bean đã được inject thành công
        Assert.assertNotNull(authController, "AuthController bị null!");
        Assert.assertNotNull(userRepository, "UserRepository bị null!");

        // Sử dụng ExtendedModelMap làm đối tượng Model
        model = new ExtendedModelMap();
        // Sử dụng MockHttpSession làm session giả
        session = new MockHttpSession();
    }

    /**
     * TC-004: Test đăng nhập thành công với tài khoản USER
     */
    @Test
    @Transactional
    public void testLoginSuccess_User() {
        // Giả sử user test đã được tạo với username "user1" và password "123"
        String view = authController.login("user1", "123", session, model);

        // Kiểm tra kết quả trả về chuyển hướng đến trang home
        Assert.assertEquals(view, "redirect:/home", "Redirect URL không khớp cho user!");

        // Kiểm tra user đã được lưu vào session với key "user"
        User userInSession = (User) session.getAttribute("user");
        Assert.assertNotNull(userInSession, "Không có user được lưu trong session!");

        // Kiểm tra username trong session
        Assert.assertEquals(userInSession.getUsername(), "user1", "Username trong session không khớp!");
    }


    /**
     * TC-005: Test đăng nhập thành công với tài khoản ADMIN
     */
    @Test
    @Transactional
    public void testLoginSuccess_Admin() {
        String view = authController.login("admin", "admin123", session, model);
        // Kiểm tra kết quả trả về chuyển hướng đến trang admin
        Assert.assertEquals(view, "redirect:/admin/index", "Redirect URL không khớp cho admin!");
        // Kiểm tra user đã được lưu vào session
        User userInSession = (User) session.getAttribute("user");
        Assert.assertNotNull(userInSession, "Không có user được lưu trong session!");
        Assert.assertEquals(userInSession.getUsername(), "admin", "Username trong session không khớp!");
    }

    /**
     * TC-006: Test đăng nhập thất bại do sai mật khẩu
     */
    @Test
    @Transactional
    public void testLoginFailure_WrongPassword() {
        String view = authController.login("user4", "125", session, model);
        // Kiểm tra trả về giao diện đăng nhập
        Assert.assertEquals(view, "/web/login", "Giao diện trả về không khớp khi đăng nhập thất bại!");
        // Kiểm tra thông báo lỗi trong model
        Assert.assertEquals(model.getAttribute("message"), "Sai tài khoản hoặc mật khẩu!");
        // Kiểm tra session không chứa user
        Assert.assertNull(session.getAttribute("user"), "Session không nên có user khi đăng nhập thất bại!");
    }

}
