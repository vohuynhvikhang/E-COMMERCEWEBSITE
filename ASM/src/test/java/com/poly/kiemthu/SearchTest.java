package com.poly.kiemthu;

import com.poly.asm.AsmApplication;
import com.poly.asm.controller.ProductController;
import com.poly.asm.daos.CategoryRepository;
import com.poly.asm.daos.ProductRepository;
import com.poly.asm.entitys.Product;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.test.context.testng.AbstractTestNGSpringContextTests;
import org.springframework.ui.ExtendedModelMap;
import org.springframework.ui.Model;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import jakarta.transaction.Transactional;
import java.util.List;

@SpringBootTest(classes = AsmApplication.class)
public class SearchTest extends AbstractTestNGSpringContextTests {

    @Autowired
    private ProductController productController;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    private Model model;

    @BeforeMethod
    public void setUp() {
        // Khởi tạo model
        model = new ExtendedModelMap();
    }

    /**
     * TC-010: Tìm kiếm sản phẩm với từ khóa chính xác "Áo khoác"
     */
    @Test
    @Transactional
    public void testSearchProduct_ExactKeyword() {
        String keyword = "ÁO KHOÁC LENUP MÙA ĐÔNG";
        String view = productController.searchProducts(keyword, 0, 100, model);
        Assert.assertEquals(view, "/web/product", "View trả về không khớp cho tìm kiếm sản phẩm!");

        // Lấy danh sách sản phẩm từ model
        Object productPageObj = model.getAttribute("productPage");
        Assert.assertNotNull(productPageObj, "productPage không có trong model!");
        Assert.assertTrue(productPageObj instanceof Page, "productPage không phải là kiểu Page<Product>!");
        Page<?> productPage = (Page<?>) productPageObj;

        // Kiểm tra rằng tất cả sản phẩm có tên chính xác "Áo khoác"
        productPage.getContent().forEach(obj -> {
            Product product = (Product) obj;
            Assert.assertEquals(product.getName(), keyword, "Sản phẩm không khớp từ khóa chính xác!");
        });
    }

    /**
     * TC-011: Tìm kiếm sản phẩm với từ khóa một phần "Áo"
     */
    @Test
    @Transactional
    public void testSearchProduct_PartialKeyword() {
        String keyword = "Áo";
        String view = productController.searchProducts(keyword, 0, 100, model);
        Assert.assertEquals(view, "/web/product", "View trả về không khớp cho tìm kiếm sản phẩm!");

        Object productPageObj = model.getAttribute("productPage");
        Assert.assertNotNull(productPageObj, "productPage không có trong model!");
        Assert.assertTrue(productPageObj instanceof Page, "productPage không phải là kiểu Page<Product>!");
        Page<?> productPage = (Page<?>) productPageObj;

        // Kiểm tra rằng tên của các sản phẩm chứa từ "Áo"
        productPage.getContent().forEach(obj -> {
            Product product = (Product) obj;
            Assert.assertTrue(product.getName().toLowerCase().contains(keyword.toLowerCase()),
                    "Sản phẩm '" + product.getName() + "' không chứa từ khóa '" + keyword + "'!");
        });
    }

    /**
     * TC-012: Tìm kiếm sản phẩm không phân biệt chữ hoa/chữ thường với từ khóa "áo khoác"
     */
    @Test
    @Transactional
    public void testSearchProduct_CaseInsensitive() {
        String keyword = "áo khoác"; // chữ thường
        String view = productController.searchProducts(keyword, 0, 100, model);
        Assert.assertEquals(view, "/web/product", "View trả về không khớp cho tìm kiếm sản phẩm!");

        Object productPageObj = model.getAttribute("productPage");
        Assert.assertNotNull(productPageObj, "productPage không có trong model!");
        Assert.assertTrue(productPageObj instanceof Page, "productPage không phải là kiểu Page<Product>!");
        Page<?> productPage = (Page<?>) productPageObj;

        // Kiểm tra rằng tên của các sản phẩm (đã được tìm kiếm không phân biệt chữ hoa/chữ thường) chứa "Áo khoác"
        productPage.getContent().forEach(obj -> {
            Product product = (Product) obj;
            Assert.assertTrue(product.getName().toLowerCase().contains("áo khoác".toLowerCase()),
                    "Sản phẩm '" + product.getName() + "' không khớp với tìm kiếm không phân biệt chữ hoa/chữ thường!");
        });
    }

    /**
     * TC-013: Tìm kiếm sản phẩm với từ khóa không tồn tại "Áo polo"
     */
    @Test
    @Transactional
    public void testSearchProduct_NonExistentKeyword() {
        String keyword = "Áo polo"; // Giả sử không có sản phẩm nào chứa từ khóa này
        String view = productController.searchProducts(keyword, 0, 100, model);
        Assert.assertEquals(view, "/web/product", "View trả về không khớp cho tìm kiếm sản phẩm!");

        Object productPageObj = model.getAttribute("productPage");
        Assert.assertNotNull(productPageObj, "productPage không có trong model!");
        Assert.assertTrue(productPageObj instanceof Page, "productPage không phải là kiểu Page<Product>!");
        Page<?> productPage = (Page<?>) productPageObj;

        // Kiểm tra rằng tổng số sản phẩm là 0
        Assert.assertEquals(productPage.getTotalElements(), 0, "Có sản phẩm không nên có khi tìm kiếm với từ khóa không tồn tại!");

        // Nếu giao diện hiển thị thông báo, có thể kiểm tra model có chứa thông báo đó (nếu controller thêm attribute thông báo)
    }
}
