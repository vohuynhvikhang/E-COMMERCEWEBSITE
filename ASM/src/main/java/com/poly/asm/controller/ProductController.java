package com.poly.asm.controller;

import com.poly.asm.daos.CategoryRepository;
import com.poly.asm.daos.ProductRepository;
import com.poly.asm.entitys.Category;
import com.poly.asm.entitys.Product;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

import java.text.DecimalFormat;
import java.util.List;

@Controller
public class ProductController {

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @GetMapping("/product")
    public String showAllProducts(HttpSession session, Model model, 
                                  @RequestParam(defaultValue = "0") int page,
                                  @RequestParam(defaultValue = "6") int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<Product> productPage = productRepository.findAll(pageable);
        
        model.addAttribute("productPage", productPage);
        model.addAttribute("categories", categoryRepository.findAll());

        // Lấy user từ session và truyền vào model
        Object user = session.getAttribute("user");
        model.addAttribute("user", user);

        return "web/product";
    }


    // Hiển thị danh sách sản phẩm theo danh mục với phân trang
    @GetMapping("/product/category/{categoryId}")
    public String showProductsByCategory(@PathVariable Long categoryId, 
                                         Model model,
                                         @RequestParam(defaultValue = "0") int page,
                                         @RequestParam(defaultValue = "6") int size,
                                         HttpSession session) { // Thêm HttpSession
        Pageable pageable = PageRequest.of(page, size);
        Page<Product> productPage = productRepository.findByCategoryId(categoryId, pageable);

        model.addAttribute("productPage", productPage);
        model.addAttribute("selectedCategory", categoryId);

        List<Category> categories = categoryRepository.findAll();
        model.addAttribute("categories", categories);

        // Lấy user từ session và truyền vào model
        Object user = session.getAttribute("user");
        model.addAttribute("user", user);

        return "web/product";
    }

    // Hiển thị chi tiết sản phẩm
    @GetMapping("/product/detail/{id}")
    public String viewProductDetail(@PathVariable("id") Long id, Model model, HttpSession session) { // Thêm HttpSession
        Product product = productRepository.findById(id).orElse(null);

        if (product == null) {
            return "redirect:/product";
        }

        DecimalFormat df = new DecimalFormat("#,##0");
        String formattedPrice = df.format(product.getPrice());

        model.addAttribute("product", product);
        model.addAttribute("formattedPrice", formattedPrice);

        List<Product> relatedProducts = productRepository.findByCategoryId(product.getCategory().getId());
        model.addAttribute("relatedProducts", relatedProducts);

        // Lấy user từ session và truyền vào model
        Object user = session.getAttribute("user");
        model.addAttribute("user", user);

        return "web/productdetail";
    }
    
    @GetMapping("/product/search")
    public String searchProducts(@RequestParam("keyword") String keyword, 
                                 @RequestParam(defaultValue = "0") int page,
                                 @RequestParam(defaultValue = "6") int size, 
                                 Model model, 
                                 HttpSession session) { // Thêm HttpSession
        Pageable pageable = PageRequest.of(page, size);
        Page<Product> productPage = productRepository.findByNameContainingIgnoreCase(keyword, pageable);

        model.addAttribute("productPage", productPage);

        List<Category> categories = categoryRepository.findAll();
        model.addAttribute("categories", categories);
        model.addAttribute("keyword", keyword);

        // Lấy user từ session và truyền vào model
        Object user = session.getAttribute("user");
        model.addAttribute("user", user);

        return "web/product";
    }
}
