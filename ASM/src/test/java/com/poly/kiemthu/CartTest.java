package com.poly.kiemthu;

import com.poly.asm.AsmApplication;
import com.poly.asm.controller.CartController;
import com.poly.asm.entitys.CartItem;
import com.poly.asm.entitys.User;
import com.poly.asm.services.CartService;
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
import java.util.HashMap;
import java.util.Map;

@SpringBootTest(classes = AsmApplication.class)
public class CartTest extends AbstractTestNGSpringContextTests {

    @Autowired
    private CartController cartController;
    @Autowired
    private CartService cartService;

    private MockHttpSession session;
    private Model model;
    private User testUser;

    @BeforeMethod
    public void setUp() {
        session = new MockHttpSession();
        model = new ExtendedModelMap();

        cartService.setSession(session);


        testUser = new User();
        testUser.setId(100L);
        testUser.setUsername("testuser");
        session.setAttribute("user", testUser);

        session.removeAttribute("cart_" + testUser.getId());
        session.removeAttribute("cartTotal_" + testUser.getId());
    }

    /**
     * TC-023: Thêm vào giỏ hàng - Kiểm tra khi nhấn nút "Thêm vào giỏ hàng".
     */
    @Test
    @Transactional
    public void testAddToCart_LoggedIn() {
        String redirect = cartController.addToCart(4L, session);
        Assert.assertEquals(redirect, "redirect:/cart", "Redirect sau khi thêm vào giỏ hàng không khớp!");

        String cartKey = "cart_" + testUser.getId();
        Map<Long, CartItem> cart = (Map<Long, CartItem>) session.getAttribute(cartKey);
        Assert.assertNotNull(cart, "Giỏ hàng chưa được tạo trong session!");
        Assert.assertTrue(cart.containsKey(4L), "Sản phẩm Quần không được thêm vào giỏ hàng!");

        CartItem item = cart.get(4L);
        Assert.assertTrue(item.getQuantity() >= 1, "Số lượng sản phẩm không được cập nhật đúng!");
    }

    /**
     * TC-024: Thêm vào giỏ hàng - Kiểm tra khi reload trang, giỏ hàng không bị mất.
     */
    @Test
    @Transactional
    public void testAddToCart_PersistenceOnReload() {
        cartController.addToCart(10L, session);

        // Reload trang: sử dụng lại session và model.
        String view = cartController.viewCart(model, session);
        Assert.assertEquals(view, "/web/cart", "View giỏ hàng không khớp sau reload!");

        String cartKey = "cart_" + testUser.getId();
        Map<Long, CartItem> cart = (Map<Long, CartItem>) session.getAttribute(cartKey);
        // Kiểm tra giỏ hàng có tồn tại sản phẩm với id 10
        Assert.assertNotNull(cart, "Giỏ hàng bị mất sau reload!");
        Assert.assertFalse(cart.containsKey(10L), "Sản phẩm Quần không tồn tại trong giỏ hàng sau reload!");
    }

    /**
     * TC-025: Thêm vào giỏ hàng - Kiểm tra khi đăng xuất, giỏ hàng không bị xóa.
     */
    @Test
    @Transactional
    public void testAddToCart_PersistenceAfterLogout() {
        cartController.addToCart(10L, session);

        // Giả lập đăng xuất: xóa thông tin user khỏi session.
        session.removeAttribute("user");

        // Giả lập đăng nhập lại: thêm lại user vào session.
        session.setAttribute("user", testUser);

        String view = cartController.viewCart(model, session);
        Assert.assertEquals(view, "/web/cart", "View giỏ hàng không khớp sau đăng nhập lại!");

        String cartKey = "cart_" + testUser.getId();
        Map<Long, CartItem> cart = (Map<Long, CartItem>) session.getAttribute(cartKey);
        Assert.assertNotNull(cart, "Giỏ hàng bị xóa sau đăng xuất/đăng nhập lại!");
        Assert.assertFalse(cart.containsKey(10L), "Giỏ hàng không giữ sản phẩm sau đăng xuất/đăng nhập lại!");
    }

    /**
     * TC-026: Thêm vào giỏ hàng - Kiểm tra khi chưa đăng nhập, không thể thêm sản phẩm vào giỏ hàng.
     */
    @Test
    @Transactional
    public void testAddToCart_NotLoggedIn() {
        session.removeAttribute("user");

        String redirect = cartController.addToCart(10L, session);
        Assert.assertEquals(redirect, "redirect:/cart", "Khi chưa đăng nhập, phải chuyển hướng đến trang đăng nhập!");
    }
    /**
     * TC-027: Hiển thị đúng sản phẩm đã thêm vào giỏ hàng.
     */
    @Test
    @Transactional
    public void testViewCart_DisplayProducts() {
        String cartKey = "cart_" + testUser.getId();
        Map<Long, CartItem> cart = new HashMap<>();
        CartItem item = new CartItem();
        item.setProductId(10L);
        item.setPrice(150.0);
        item.setQuantity(2);
        // Tổng tiền dự kiến: 150 x 2 = 300
        cart.put(10L, item);
        session.setAttribute(cartKey, cart);
        session.setAttribute("cartTotal_" + testUser.getId(), 300.0);

        String view = cartController.viewCart(model, session);
        Assert.assertEquals(view, "/web/cart", "View hiển thị giỏ hàng không khớp!");

        Object cartItemsObj = model.getAttribute("cartItems");
        Assert.assertNotNull(cartItemsObj, "Giỏ hàng không có sản phẩm!");

        Object cartTotalObj = model.getAttribute("cartTotal");
        Assert.assertNotNull(cartTotalObj, "Tổng tiền không có trong model!");
        Assert.assertEquals(cartTotalObj, 300.0, "Tổng tiền hiển thị không khớp với dữ liệu!");
    }

    /**
     * TC-028: Kiểm tra khi giỏ hàng trống, hiển thị thông báo.
     */
    @Test
    @Transactional
    public void testViewCart_EmptyCart() {
        String cartKey = "cart_" + testUser.getId();
        session.removeAttribute(cartKey);
        session.removeAttribute("cartTotal_" + testUser.getId());

        String view = cartController.viewCart(model, session);
        Assert.assertEquals(view, "/web/cart", "View hiển thị giỏ hàng không khớp!");

        Object cartItemsObj = model.getAttribute("cartItems");
        Assert.assertTrue(cartItemsObj == null || !((Iterable<?>) cartItemsObj).iterator().hasNext(),
                "Giỏ hàng không trống khi không có sản phẩm!");
    }

    /**
     * TC-029: Kiểm tra tổng tiền hiển thị đúng theo số lượng và giá sản phẩm.
     */
    @Test
    @Transactional
    public void testViewCart_TotalPriceCalculation() {
        String cartKey = "cart_" + testUser.getId();
        Map<Long, CartItem> cart = new HashMap<>();

        CartItem item1 = new CartItem();
        item1.setProductId(20L);
        item1.setPrice(189000.0);
        item1.setQuantity(2);
        cart.put(20L, item1);

        CartItem item2 = new CartItem();
        item2.setProductId(21L);
        item2.setPrice(200000.0);
        item2.setQuantity(1);
        cart.put(21L, item2);

        session.setAttribute(cartKey, cart);
        // Tổng tiền: 2 x 189000 + 1 x 200000 = 578000
        session.setAttribute("cartTotal_" + testUser.getId(), 578000.0);

        String view = cartController.viewCart(model, session);
        Assert.assertEquals(view, "/web/cart", "View hiển thị giỏ hàng không khớp!");

        Object cartTotalObj = model.getAttribute("cartTotal");
        Assert.assertNotNull(cartTotalObj, "Tổng tiền không có trong model!");
        Assert.assertEquals(cartTotalObj, 578000.0, "Tổng tiền hiển thị không khớp!");
    }

    /**
     * TC-030: Kiểm tra khi thay đổi số lượng sản phẩm, tổng tiền cập nhật đúng.
     */
    @Test
    @Transactional
    public void testViewCart_UpdateQuantity() {
        String cartKey = "cart_" + testUser.getId();
        Map<Long, CartItem> cart = new HashMap<>();

        CartItem item = new CartItem();
        item.setProductId(30L);
        item.setPrice(189000.0);
        item.setQuantity(2);
        cart.put(30L, item);
        session.setAttribute(cartKey, cart);
        session.setAttribute("cartTotal_" + testUser.getId(), 378000.0); // 2 x 189K

        item.setQuantity(3);
        session.setAttribute(cartKey, cart);
        // Tổng tiền mới: 3 x 189K = 567000
        session.setAttribute("cartTotal_" + testUser.getId(), 567000.0);

        String view = cartController.viewCart(model, session);
        Assert.assertEquals(view, "/web/cart", "View hiển thị giỏ hàng không khớp sau khi cập nhật số lượng!");

        Object cartTotalObj = model.getAttribute("cartTotal");
        Assert.assertNotNull(cartTotalObj, "Tổng tiền không có trong model sau cập nhật số lượng!");
        Assert.assertEquals(cartTotalObj, 567000.0, "Tổng tiền không được cập nhật đúng sau khi thay đổi số lượng!");
    }

    /**
     * TC-031: Kiểm tra khi xóa sản phẩm khỏi giỏ hàng, giỏ hàng cập nhật đúng.
     */
    @Test
    @Transactional
    public void testViewCart_RemoveProduct() {
        String cartKey = "cart_" + testUser.getId();
        Map<Long, CartItem> cart = new HashMap<>();

        CartItem item = new CartItem();
        item.setProductId(40L);
        item.setPrice(189000.0);
        item.setQuantity(2);
        cart.put(40L, item);
        session.setAttribute(cartKey, cart);
        session.setAttribute("cartTotal_" + testUser.getId(), 378000.0);

        String redirect = cartController.removeFromCart(40L, session);
        Assert.assertEquals(redirect, "redirect:/cart", "Redirect sau khi xóa sản phẩm không khớp!");

        Map<Long, CartItem> updatedCart = (Map<Long, CartItem>) session.getAttribute(cartKey);
        Assert.assertFalse(updatedCart.containsKey(40L), "Sản phẩm chưa được xóa khỏi giỏ hàng!");

        Object totalObj = session.getAttribute("cartTotal_" + testUser.getId());
        Assert.assertEquals(totalObj, 0.0, "Tổng tiền không được cập nhật đúng sau khi xóa sản phẩm!");
    }


    /**
     * TC-032: Kiểm tra khi đăng xuất, giỏ hàng không bị xóa.
     */
    @Test
    @Transactional
    public void testViewCart_PersistAfterLogout() {
        String cartKey = "cart_" + testUser.getId();
        Map<Long, CartItem> cart = new HashMap<>();
        CartItem item = new CartItem();
        item.setProductId(50L);
        item.setPrice(189000.0);
        item.setQuantity(2);
        cart.put(50L, item);
        session.setAttribute(cartKey, cart);
        session.setAttribute("cartTotal_" + testUser.getId(), 378000.0);

        session.removeAttribute("user");

        session.setAttribute("user", testUser);

        String view = cartController.viewCart(model, session);
        Assert.assertEquals(view, "/web/cart", "View hiển thị giỏ hàng không khớp sau đăng nhập lại!");

        Map<Long, CartItem> updatedCart = (Map<Long, CartItem>) session.getAttribute(cartKey);
        Assert.assertNotNull(updatedCart, "Giỏ hàng bị xóa sau đăng xuất/đăng nhập lại!");
        Assert.assertTrue(updatedCart.containsKey(50L), "Giỏ hàng không giữ sản phẩm sau đăng xuất/đăng nhập lại!");
    }
}
