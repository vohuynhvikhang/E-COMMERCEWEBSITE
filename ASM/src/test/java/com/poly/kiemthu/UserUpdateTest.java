package com.poly.kiemthu;

import com.poly.asm.AsmApplication;
import com.poly.asm.controller.UserController;
import com.poly.asm.daos.UserRepository;
import com.poly.asm.entitys.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.context.testng.AbstractTestNGSpringContextTests;
import org.springframework.ui.ExtendedModelMap;
import org.springframework.ui.Model;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import jakarta.transaction.Transactional;

@SpringBootTest(classes = AsmApplication.class)
public class UserUpdateTest extends AbstractTestNGSpringContextTests {

    @Autowired
    private UserController userController;

    @Autowired
    private UserRepository userRepository;

    private MockHttpSession session;
    private Model model;
    private User testUser;

    @BeforeMethod
    public void setUp() {
        session = new MockHttpSession();
        model = new ExtendedModelMap();

        if(userRepository.existsById(100L)) {
            userRepository.deleteById(100L);
        }

        testUser = new User();
        testUser.setUsername("testuser");
        testUser.setFullname("Old Name");
        testUser.setPhone("0123456789");
        testUser.setPassword("oldpassword");
        testUser.setEmail("testuser@example.com");
        testUser = userRepository.save(testUser);

        session.setAttribute("user", testUser);
    }

    /**
     * TC-033: Cập nhật thông tin thành công
     */
    @Test
    @Transactional
    public void testUpdateProfile_Success() {
        String newFullName = "Vĩ Khang dz";
        String newPhone = "0987654321";
        String newPassword = "123456";

        String redirect = userController.updateProfile(newFullName, newPhone, newPassword, session, model);

        Assert.assertEquals(redirect, "redirect:/home", "Redirect sau cập nhật không khớp!");

        Object message = model.getAttribute("message");
        Assert.assertNotNull(message, "Không có thông báo cập nhật thành công!");
        Assert.assertEquals(message, "Cập nhật thành công!", "Thông báo cập nhật không khớp!");

        User updatedUserSession = (User) session.getAttribute("user");
        Assert.assertNotNull(updatedUserSession, "User không có trong session sau cập nhật!");
        Assert.assertEquals(updatedUserSession.getFullname(), newFullName, "Họ và tên không được cập nhật trong session!");
        Assert.assertEquals(updatedUserSession.getPhone(), newPhone, "Số điện thoại không được cập nhật trong session!");
        Assert.assertEquals(updatedUserSession.getPassword(), newPassword, "Mật khẩu không được cập nhật trong session!");

        User updatedUserDB = userRepository.findById(testUser.getId()).orElse(null);
        Assert.assertNotNull(updatedUserDB, "User không tồn tại trong DB sau cập nhật!");
        Assert.assertEquals(updatedUserDB.getFullname(), newFullName, "Họ và tên không được cập nhật trong DB!");
        Assert.assertEquals(updatedUserDB.getPhone(), newPhone, "Số điện thoại không được cập nhật trong DB!");
        Assert.assertEquals(updatedUserDB.getPassword(), newPassword, "Mật khẩu không được cập nhật trong DB!");
    }
}
