package com.poly.asm.controller;

import com.poly.asm.daos.*;
import com.poly.asm.entitys.*;
import com.poly.asm.services.CartService;

import jakarta.servlet.http.HttpServletRequest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/product")
public class ProductController {

    private static final Logger logger = LoggerFactory.getLogger(ProductController.class);
    @Autowired
    private CartService cartService;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private ProductImageRepository productImageRepository;

    @Autowired
    private ProductVariantRepository productVariantRepository;

    
    @ModelAttribute
    public void addAttributesToAllModels(HttpServletRequest request, Model model) {
        Cart cart = cartService.getCart(request); // Đúng
        int cartItemCount = cart != null ? cart.getCartItems().size() : 0;
        model.addAttribute("cartItemCount", cartItemCount);
    }
    
    @GetMapping
    public String showAllProducts(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "9") int size,
            Model model) {
        Pageable pageable = PageRequest.of(page, size);
        Page<Product> productPage = productRepository.findAll(pageable);
        List<Product> products = productPage.getContent();

        // Tạo imageUrls
        Map<Long, String> imageUrls = products.stream()
                .collect(Collectors.toMap(
                        Product::getId,
                        p -> productImageRepository.findPrimaryImageByProductId(p.getId())
                                .map(ProductImage::getImageUrl)
                                .orElse("/path/to/product.jpg"),
                        (existing, newValue) -> existing
                ));

        // Tạo formattedPrices
        DecimalFormat df = new DecimalFormat("#,##0");
        Map<Long, String> formattedPrices = products.stream()
                .collect(Collectors.toMap(
                        Product::getId,
                        p -> {
                            List<ProductVariant> variants = productVariantRepository.findByProductId(p.getId());
                            if (variants.isEmpty()) {
                                return "0";
                            }
                            BigDecimal minPrice = variants.stream()
                                    .map(ProductVariant::getPrice)
                                    .filter(v -> v != null)
                                    .min(BigDecimal::compareTo)
                                    .orElse(BigDecimal.ZERO);
                            return df.format(minPrice);
                        },
                        (existing, newValue) -> existing
                ));

        model.addAttribute("productPage", productPage);
        model.addAttribute("categories", categoryRepository.findAll());
        model.addAttribute("imageUrls", imageUrls);
        model.addAttribute("formattedPrices", formattedPrices);
        model.addAttribute("selectedCategory", null);

        return "web/product";
    }

    @GetMapping("/category/{id}")
    public String showProductsByCategory(
            @PathVariable("id") Long categoryId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "9") int size,
            Model model) {
        Pageable pageable = PageRequest.of(page, size);
        Page<Product> productPage = productRepository.findByCategoryId(categoryId, pageable);
        List<Product> products = productPage.getContent();

        // Tạo imageUrls
        Map<Long, String> imageUrls = products.stream()
                .collect(Collectors.toMap(
                        Product::getId,
                        p -> productImageRepository.findPrimaryImageByProductId(p.getId())
                                .map(ProductImage::getImageUrl)
                                .orElse("/path/to/product.jpg"),
                        (existing, newValue) -> existing
                ));

        // Tạo formattedPrices
        DecimalFormat df = new DecimalFormat("#,##0");
        Map<Long, String> formattedPrices = products.stream()
                .collect(Collectors.toMap(
                        Product::getId,
                        p -> {
                            List<ProductVariant> variants = productVariantRepository.findByProductId(p.getId());
                            if (variants.isEmpty()) {
                                return "0";
                            }
                            BigDecimal minPrice = variants.stream()
                                    .map(ProductVariant::getPrice)
                                    .filter(v -> v != null)
                                    .min(BigDecimal::compareTo)
                                    .orElse(BigDecimal.ZERO);
                            return df.format(minPrice);
                        },
                        (existing, newValue) -> existing
                ));

        model.addAttribute("productPage", productPage);
        model.addAttribute("categories", categoryRepository.findAll());
        model.addAttribute("imageUrls", imageUrls);
        model.addAttribute("formattedPrices", formattedPrices);
        model.addAttribute("selectedCategory", categoryId);

        return "web/product";
    }

    @GetMapping("/detail/{id}")
    public String viewProductDetail(@PathVariable("id") Long id, Model model) {
        Product product = productRepository.findByIdWithPrimaryImages(id);
        if (product == null) {
            logger.warn("Product not found for ID: {}", id);
            return "redirect:/product";
        }

        List<ProductImage> images = productImageRepository.findByProductId(id);
        List<ProductVariant> variants = productVariantRepository.findByProductId(id);

        // Log để debug
        logger.info("Product ID: {}, Images: {}, Variants: {}", id, images.size(), variants.size());

        // Lấy sản phẩm liên quan
        List<Product> relatedProducts = productRepository.findByCategoryIdAndIdNot(
                product.getCategory().getId(), id, PageRequest.of(0, 4)).getContent();

        // Tạo relatedProductImages
        Map<Long, String> relatedProductImages = relatedProducts.stream()
                .collect(Collectors.toMap(
                        Product::getId,
                        p -> productImageRepository.findPrimaryImageByProductId(p.getId())
                                .map(ProductImage::getImageUrl)
                                .orElse("/path/to/product.jpg"),
                        (existing, newValue) -> existing
                ));

        // Tạo relatedProductPrices
        DecimalFormat df = new DecimalFormat("#,##0");
        Map<Long, String> relatedProductPrices = relatedProducts.stream()
                .collect(Collectors.toMap(
                        Product::getId,
                        p -> {
                            List<ProductVariant> relVariants = productVariantRepository.findByProductId(p.getId());
                            if (relVariants.isEmpty()) {
                                return "0";
                            }
                            BigDecimal minPrice = relVariants.stream()
                                    .map(ProductVariant::getPrice)
                                    .filter(v -> v != null)
                                    .min(BigDecimal::compareTo)
                                    .orElse(BigDecimal.ZERO);
                            return df.format(minPrice);
                        },
                        (existing, newValue) -> existing
                ));

        model.addAttribute("product", product);
        model.addAttribute("images", images);
        model.addAttribute("variants", variants);
        model.addAttribute("relatedProducts", relatedProducts);
        model.addAttribute("relatedProductImages", relatedProductImages);
        model.addAttribute("relatedProductPrices", relatedProductPrices);

        return "web/productdetail";
    }

    @GetMapping("/search")
    public String searchProducts(
            @RequestParam("keyword") String keyword,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "9") int size,
            Model model) {
        Pageable pageable = PageRequest.of(page, size);
        Page<Product> productPage = productRepository.findByNameContainingIgnoreCase(keyword, pageable);
        List<Product> products = productPage.getContent();

        // Tạo imageUrls
        Map<Long, String> imageUrls = products.stream()
                .collect(Collectors.toMap(
                        Product::getId,
                        p -> productImageRepository.findPrimaryImageByProductId(p.getId())
                                .map(ProductImage::getImageUrl)
                                .orElse("/path/to/product.jpg"),
                        (existing, newValue) -> existing
                ));

        // Tạo formattedPrices
        DecimalFormat df = new DecimalFormat("#,##0");
        Map<Long, String> formattedPrices = products.stream()
                .collect(Collectors.toMap(
                        Product::getId,
                        p -> {
                            List<ProductVariant> variants = productVariantRepository.findByProductId(p.getId());
                            if (variants.isEmpty()) {
                                return "0";
                            }
                            BigDecimal minPrice = variants.stream()
                                    .map(ProductVariant::getPrice)
                                    .filter(v -> v != null)
                                    .min(BigDecimal::compareTo)
                                    .orElse(BigDecimal.ZERO);
                            return df.format(minPrice);
                        },
                        (existing, newValue) -> existing
                ));

        model.addAttribute("productPage", productPage);
        model.addAttribute("categories", categoryRepository.findAll());
        model.addAttribute("imageUrls", imageUrls);
        model.addAttribute("formattedPrices", formattedPrices);
        model.addAttribute("keyword", keyword);
        model.addAttribute("selectedCategory", null);

        return "web/product";
    }
}