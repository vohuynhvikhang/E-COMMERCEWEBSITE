package com.poly.kiemthu;

import com.poly.asm.AsmApplication;
import com.poly.asm.controller.ProductController;
import com.poly.asm.daos.CategoryRepository;
import com.poly.asm.daos.ProductRepository;
import com.poly.asm.entitys.Category;
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

import java.text.DecimalFormat;
import java.util.List;

@SpringBootTest(classes = AsmApplication.class)
public class ProductListTest extends AbstractTestNGSpringContextTests {

    @Autowired
    private ProductController productController;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    private Model model;

    @BeforeMethod
    public void setUp() {
        model = new ExtendedModelMap();
    }

    /**
     * TC-007: Kiểm tra hiển thị danh sách sản phẩm đúng dữ liệu trong database
     */
    @Test
    @Transactional
    public void testShowAllProducts() {
        // Gọi controller với các giá trị mặc định cho phân trang
        String view = productController.showAllProducts(model, 0, 100); // 100 để đảm bảo lấy hết dữ liệu nếu số lượng ít
        Assert.assertEquals(view, "/web/product", "View trả về không khớp cho showAllProducts!");

        // Kiểm tra model có chứa đối tượng productPage
        Object productPageObj = model.getAttribute("productPage");
        Assert.assertNotNull(productPageObj, "productPage không có trong model!");
        Assert.assertTrue(productPageObj instanceof Page, "productPage không phải là kiểu Page<Product>!");

        // Nếu muốn kiểm tra thêm nội dung, có thể so sánh số lượng sản phẩm trả về với số lượng trong DB
        Page<?> productPage = (Page<?>) productPageObj;
        long totalProducts = productRepository.count();
        Assert.assertEquals(productPage.getTotalElements(), totalProducts, "Số lượng sản phẩm không khớp với DB!");
    }

    /**
     * TC-008: Kiểm tra khi chọn một danh mục, chỉ hiển thị sản phẩm thuộc danh mục đó
     */
    @Test
    @Transactional
    public void testFilterProductsByCategory_WithData() {
        Long categoryId = 2L; // "Áo khoác"
        String view = productController.showProductsByCategory(categoryId, model, 0, 100);
        Assert.assertEquals(view, "/web/product", "View trả về không khớp cho bộ lọc sản phẩm theo danh mục!");

        // Lấy đối tượng productPage từ model
        Object productPageObj = model.getAttribute("productPage");
        Assert.assertNotNull(productPageObj, "productPage không có trong model!");
        Assert.assertTrue(productPageObj instanceof Page, "productPage không phải là kiểu Page<Product>!");

        Page<?> productPage = (Page<?>) productPageObj;
        // Kiểm tra số lượng sản phẩm > 0
        Assert.assertTrue(productPage.getTotalElements() > 0, "Không có sản phẩm nào thuộc danh mục 'Áo khoác'!");

        // Lặp qua các sản phẩm và kiểm tra rằng category của từng sản phẩm là danh mục đã chọn
        productPage.getContent().forEach(prod -> {
            Product product = (Product) prod;
            Assert.assertEquals(product.getCategory().getId(), categoryId, "Sản phẩm thuộc danh mục 'Áo khoác'!");
        });
    }

    /**
     * TC-009: Kiểm tra khi không có sản phẩm nào thỏa điều kiện lọc
     */
    @Test
    @Transactional
    public void testFilterProductsByCategory_NoData() {
        Long categoryId = 5L; // "Áo polo"
        String view = productController.showProductsByCategory(categoryId, model, 0, 100);
        Assert.assertEquals(view, "/web/product", "View trả về không khớp cho bộ lọc sản phẩm theo danh mục!");

        // Lấy đối tượng productPage từ model
        Object productPageObj = model.getAttribute("productPage");
        Assert.assertNotNull(productPageObj, "productPage không có trong model!");
        Assert.assertTrue(productPageObj instanceof Page, "productPage không phải là kiểu Page<Product>!");

        Page<?> productPage = (Page<?>) productPageObj;
        // Kiểm tra rằng không có sản phẩm nào thỏa điều kiện
        Assert.assertEquals(productPage.getTotalElements(), 0, "Không có sản phẩm nào thuộc danh mục 'Áo polo'!");
    }

    /**
     * TC-020: Kiểm tra khi nhấn vào sản phẩm, trang chi tiết hiển thị đúng thông tin.
     */
    @Test
    @Transactional
    public void testProductDetail_DisplayCorrectInformation() {
        Long productId = 1L;
        Product expectedProduct = productRepository.findById(productId).orElse(null);
        Assert.assertNotNull(expectedProduct, "Sản phẩm không tồn tại trong DB!");

        String view = productController.viewProductDetail(productId, model);
        Assert.assertEquals(view, "/web/productdetail", "View trả về không khớp!");

        Product actualProduct = (Product) model.getAttribute("product");
        Assert.assertNotNull(actualProduct, "Sản phẩm không có trong model!");
        Assert.assertEquals(actualProduct.getName(), expectedProduct.getName(), "Tên sản phẩm không khớp!");
        Assert.assertEquals(actualProduct.getDescription(), expectedProduct.getDescription(), "Mô tả sản phẩm không khớp!");
        Assert.assertEquals(actualProduct.getImage(), expectedProduct.getImage(), "Hình ảnh sản phẩm không khớp!");

        DecimalFormat df = new DecimalFormat("#,##0");
        String expectedFormattedPrice = df.format(expectedProduct.getPrice());
        String actualFormattedPrice = (String) model.getAttribute("formattedPrice");
        Assert.assertEquals(actualFormattedPrice, expectedFormattedPrice, "Giá sản phẩm không khớp sau định dạng!");
    }

    /**
     * TC-021: Kiểm tra sản phẩm liên quan hiển thị đúng danh mục.
     */
    @Test
    @Transactional
    public void testProductDetail_RelatedProductsByCategory() {
        Long productId = 1L;
        Product product = productRepository.findById(productId).orElse(null);
        Assert.assertNotNull(product, "Sản phẩm không tồn tại trong DB!");

        Long categoryId = product.getCategory().getId();

        String view = productController.viewProductDetail(productId, model);
        Assert.assertEquals(view, "/web/productdetail", "View trả về không khớp!");

        @SuppressWarnings("unchecked")
        List<Product> relatedProducts = (List<Product>) model.getAttribute("relatedProducts");
        Assert.assertNotNull(relatedProducts, "Danh sách sản phẩm liên quan không có trong model!");

        relatedProducts.forEach(related -> {
            Assert.assertEquals(related.getCategory().getId(), categoryId,
                    "Sản phẩm liên quan không thuộc danh mục 'Áo thun'!");
        });
    }

    /**
     * TC-022: Kiểm tra khi danh mục chỉ có 1 sản phẩm.
     */
    @Test
    @Transactional
    public void testProductDetail_SingleProductInCategory() {
        Long productId = 3L;
        Product product = productRepository.findById(productId).orElse(null);
        Assert.assertNotNull(product, "Sản phẩm không tồn tại trong DB!");

        Long categoryId = product.getCategory().getId();
        List<Product> productsInCategory = productRepository.findByCategoryId(categoryId);
        Assert.assertEquals(productsInCategory.size(), 2, "Danh mục 'Quần' có nhiều hơn 1 sản phẩm!");

        String view = productController.viewProductDetail(productId, model);
        Assert.assertEquals(view, "/web/productdetail", "View trả về không khớp!");

        @SuppressWarnings("unchecked")
        List<Product> relatedProducts = (List<Product>) model.getAttribute("relatedProducts");
        Assert.assertFalse(relatedProducts.isEmpty(), "Phần sản phẩm liên quan không trống khi danh mục chỉ có 1 sản phẩm!");
    }
}
