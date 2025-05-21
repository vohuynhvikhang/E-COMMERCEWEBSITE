package com.poly.kiemthu;

import com.poly.asm.AsmApplication;
import com.poly.asm.controller.ProductController;
import com.poly.asm.daos.ProductRepository;
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

@SpringBootTest(classes = AsmApplication.class)
public class ProductPaginationTest extends AbstractTestNGSpringContextTests {

    @Autowired
    private ProductController productController;

    @Autowired
    private ProductRepository productRepository;

    private Model model;

    @BeforeMethod
    public void setUp() {
        model = new ExtendedModelMap();
    }

    /**
     * TC-014: Kiểm tra khi nhấn số trang, dữ liệu hiển thị đúng với dữ liệu của trang đó
     */
    @Test
    @Transactional
    public void testPagination_Page2() {
        int page = 1;
        int size = 6;
        String view = productController.showAllProducts(model, page, size);
        Assert.assertEquals(view, "/web/product", "View không khớp cho phân trang trang 2!");

        Object productPageObj = model.getAttribute("productPage");
        Assert.assertNotNull(productPageObj, "productPage không có trong model!");
        Assert.assertTrue(productPageObj instanceof Page, "productPage không phải là kiểu Page<Product>!");
        Page<?> productPage = (Page<?>) productPageObj;

        Assert.assertTrue(productPage.getContent().size() <= size, "Số sản phẩm trên trang vượt quá giới hạn!");
    }

    /**
     * TC-015: Kiểm tra khi không có sản phẩm nào, phân trang không hiển thị
     */
    @Test
    @Transactional
    public void testPagination_NoProducts() {
        Long categoryId = 999L;
        String view = productController.showProductsByCategory(categoryId, model, 0, 6);
        Assert.assertEquals(view, "/web/product", "View trả về không khớp cho bộ lọc sản phẩm theo danh mục!");

        Object productPageObj = model.getAttribute("productPage");
        Assert.assertNotNull(productPageObj, "productPage không có trong model!");
        Assert.assertTrue(productPageObj instanceof org.springframework.data.domain.Page, "productPage không phải kiểu Page<Product>!");
        org.springframework.data.domain.Page<?> productPage = (org.springframework.data.domain.Page<?>) productPageObj;

        Assert.assertEquals(productPage.getTotalElements(), 0, "Số sản phẩm không bằng 0 cho danh mục không có sản phẩm!");
    }

    /**
     * TC-016: Kiểm tra khi có đúng 1 trang, phân trang không xuất hiện
     */
    @Test
    @Transactional
    public void testPagination_SinglePage() {
        long total = productRepository.count();
        if(total < 1 || total > 6) {
            System.out.println("Không thể kiểm tra trường hợp 1 trang vì số sản phẩm = " + total);
            return;
        }
        int page = 0;
        int size = 6;
        String view = productController.showAllProducts(model, page, size);
        Assert.assertEquals(view, "/web/product", "View không khớp cho phân trang ở trang đầu!");

        Object productPageObj = model.getAttribute("productPage");
        Assert.assertNotNull(productPageObj, "productPage không có trong model!");
        Assert.assertTrue(productPageObj instanceof Page, "productPage không phải là kiểu Page<Product>!");
        Page<?> productPage = (Page<?>) productPageObj;

        Assert.assertEquals(productPage.getTotalPages(), 1, "Có nhiều hơn 1 trang khi chỉ có ≤ 6 sản phẩm!");
    }

    /**
     * TC-017: Kiểm tra khi có nhiều trang, nút "Trang tiếp theo" và "Trang trước đó" hiển thị
     */
    @Test
    @Transactional
    public void testPagination_MultiplePages() {
        int page = 0;
        int size = 6;
        String view = productController.showAllProducts(model, page, size);
        Assert.assertEquals(view, "/web/product", "View trả về không khớp!");

        Object productPageObj = model.getAttribute("productPage");
        Assert.assertNotNull(productPageObj, "productPage không có trong model!");
        Assert.assertTrue(productPageObj instanceof org.springframework.data.domain.Page, "productPage không phải kiểu Page<Product>!");
        org.springframework.data.domain.Page<?> productPage = (org.springframework.data.domain.Page<?>) productPageObj;

        Assert.assertTrue(productPage.getTotalPages() > 1, "Không có nhiều trang khi yêu cầu hơn 7 sản phẩm!");

        model = new ExtendedModelMap();
        String viewPage1 = productController.showAllProducts(model, 1, size);
        Object productPageObj2 = model.getAttribute("productPage");
        org.springframework.data.domain.Page<?> productPage2 = (org.springframework.data.domain.Page<?>) productPageObj2;
        Assert.assertTrue(productPage2.hasNext(), "Ở trang giữa, nút 'Trang tiếp theo' phải hiển thị!");
        Assert.assertTrue(productPage2.hasPrevious(), "Ở trang giữa, nút 'Trang trước đó' phải hiển thị!");
    }

    /**
     * TC-018: Kiểm tra khi ở trang cuối, nút "Trang tiếp theo" bị vô hiệu hóa
     */
    @Test
    @Transactional
    public void testPagination_LastPage_NoNext() {
        int size = 6;
        model = new ExtendedModelMap();
        productController.showAllProducts(model, 0, size);
        org.springframework.data.domain.Page<?> firstPage = (org.springframework.data.domain.Page<?>) model.getAttribute("productPage");
        int totalPages = firstPage.getTotalPages();

        model = new ExtendedModelMap();
        String view = productController.showAllProducts(model, totalPages - 1, size);
        Assert.assertEquals(view, "/web/product", "View không khớp khi chuyển đến trang cuối!");

        org.springframework.data.domain.Page<?> lastPage = (org.springframework.data.domain.Page<?>) model.getAttribute("productPage");
        Assert.assertFalse(lastPage.hasNext(), "Nút 'Trang tiếp theo' vẫn hiển thị ở trang cuối!");
    }

    /**
     * TC-019: Kiểm tra khi ở trang đầu, nút "Trang trước đó" bị vô hiệu hóa
     */
    @Test
    @Transactional
    public void testPagination_FirstPage_NoPrevious() {
        int page = 0;
        int size = 6;
        model = new ExtendedModelMap();
        String view = productController.showAllProducts(model, page, size);
        Assert.assertEquals(view, "/web/product", "View không khớp cho trang đầu!");

        org.springframework.data.domain.Page<?> productPage = (org.springframework.data.domain.Page<?>) model.getAttribute("productPage");
        Assert.assertFalse(productPage.hasPrevious(), "Nút 'Trang trước đó' vẫn hiển thị ở trang đầu!");
    }

}
