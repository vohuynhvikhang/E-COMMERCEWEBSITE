package com.poly.asm.controller;

import java.io.File;
import java.io.IOException;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.poly.asm.daos.CategoryRepository;
import com.poly.asm.daos.OrderRepository;
import com.poly.asm.daos.ProductRepository;
import com.poly.asm.daos.UserRepository;
import com.poly.asm.entitys.Category;
import com.poly.asm.entitys.Order;
import com.poly.asm.entitys.Product;
import com.poly.asm.entitys.User;
import com.poly.asm.services.OrderService;

import jakarta.servlet.http.HttpServletRequest;

@Controller
@RequestMapping("/admin")
public class AdminController {

	@Autowired
    private ProductRepository productRepository;

    @Autowired
    private CategoryRepository categoryRepository;
    
    @Autowired
    private UserRepository userRepository;

    @Autowired
    private OrderRepository orderRepository; // Đảm bảo đã có OrderRepository

    @Autowired
    private OrderService orderService; // Đảm bảo đã có OrderService

    // ----------- QUẢN LÝ ĐƠN HÀNG ----------- 

    // Hiển thị danh sách đơn hàng
    @GetMapping("/orders")
    public String listOrders(Model model) {
        model.addAttribute("orders", orderRepository.findAll());
        return "admin/orders";  // Thay đổi tên view nếu cần
    }

    // Hiển thị chi tiết đơn hàng
    @GetMapping("/orders/{id}")
    public String orderDetails(@PathVariable Long id, Model model, RedirectAttributes redirectAttributes) {
        Optional<Order> orderOpt = orderRepository.findById(id);
        if (!orderOpt.isPresent()) {
            redirectAttributes.addFlashAttribute("errorMessage", "Không tìm thấy đơn hàng!");
            return "redirect:/admin/orders";
        }

        model.addAttribute("order", orderOpt.get());
        return "admin/order-detail";  // Thay đổi tên view nếu cần
    }

    @PostMapping("/orders/{id}/status")
    public String updateOrderStatus(@PathVariable Long id, @RequestParam("status") String status, RedirectAttributes redirectAttributes) {
        Optional<Order> orderOpt = orderRepository.findById(id);
        if (orderOpt.isPresent()) {
            Order order = orderOpt.get();
            order.setStatus(status); // Cập nhật trạng thái
            orderRepository.save(order); // Lưu lại
            redirectAttributes.addFlashAttribute("successMessage", "Trạng thái đơn hàng đã được cập nhật!");
        } else {
            redirectAttributes.addFlashAttribute("errorMessage", "Không tìm thấy đơn hàng!");
        }
        return "redirect:/admin/orders";
    }


    // Xóa đơn hàng
    @GetMapping("/orders/delete/{id}")
    public String deleteOrder(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        Optional<Order> orderOpt = orderRepository.findById(id);
        if (!orderOpt.isPresent()) {
            redirectAttributes.addFlashAttribute("errorMessage", "Không tìm thấy đơn hàng!");
        } else {
            orderRepository.deleteById(id);
            redirectAttributes.addFlashAttribute("successMessage", "Đơn hàng đã được xóa!");
        }
        return "redirect:/admin/orders";
    }

    // ----------- HIỂN THỊ TRANG CHỦ ----------- 
    @GetMapping("/index")
    public String home() {
        return "admin/index";
    }

    // ----------- QUẢN LÝ SẢN PHẨM ----------- 
    @GetMapping("/products")
    public String listProducts(Model model) {
        model.addAttribute("products", productRepository.findAll());
        model.addAttribute("categories", categoryRepository.findAll());
        model.addAttribute("product", new Product()); // Form mặc định là sản phẩm mới
        return "admin/products";
    }

    @PostMapping("/products/save")
    public String saveProduct(@ModelAttribute Product product, @RequestParam("imageFile") MultipartFile file,
            HttpServletRequest request, RedirectAttributes redirectAttributes) {
        try {
            if (!file.isEmpty()) {
                // Đường dẫn thư mục lưu ảnh
                String uploadDir = request.getServletContext().getRealPath("/uploads/");
                File uploadFolder = new File(uploadDir);
                if (!uploadFolder.exists()) {
                    uploadFolder.mkdirs();
                }

                // Lưu file ảnh
                String fileName = file.getOriginalFilename();
                File saveFile = new File(uploadDir + fileName);
                file.transferTo(saveFile);

                // Cập nhật đường dẫn ảnh vào sản phẩm
                product.setImage("/uploads/" + fileName);
            }

            productRepository.save(product);
            redirectAttributes.addFlashAttribute("successMessage", "Sản phẩm đã được lưu thành công!");
        } catch (IOException e) {
            e.printStackTrace();
            redirectAttributes.addFlashAttribute("errorMessage", "Lỗi khi lưu ảnh sản phẩm!");
        }
        return "redirect:/admin/products";
    }

    @GetMapping("/products/delete/{id}")
    public String deleteProduct(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        if (productRepository.existsById(id)) {
            productRepository.deleteById(id);
            redirectAttributes.addFlashAttribute("successMessage", "Sản phẩm đã được xóa!");
        } else {
            redirectAttributes.addFlashAttribute("errorMessage", "Không tìm thấy sản phẩm!");
        }
        return "redirect:/admin/products";
    }

    @GetMapping("/products/edit/{id}")
    public String editProduct(@PathVariable Long id, Model model, RedirectAttributes redirectAttributes) {
        Optional<Product> productOpt = productRepository.findById(id);
        if (!productOpt.isPresent()) {
            redirectAttributes.addFlashAttribute("errorMessage", "Không tìm thấy sản phẩm!");
            return "redirect:/admin/products";
        }

        model.addAttribute("product", productOpt.get());
        model.addAttribute("categories", categoryRepository.findAll());
        return "admin/products";
    }

    // ----------- QUẢN LÝ DANH MỤC ----------- 
    @GetMapping("/categories")
    public String listCategories(Model model) {
        model.addAttribute("categories", categoryRepository.findAll());
        model.addAttribute("category", new Category()); // Form mặc định là danh mục mới
        return "admin/categories";
    }

    @PostMapping("/categories/save")
    public String saveCategory(@ModelAttribute Category category, RedirectAttributes redirectAttributes) {
        categoryRepository.save(category);
        redirectAttributes.addFlashAttribute("successMessage", "Danh mục đã được lưu thành công!");
        return "redirect:/admin/categories";
    }

    @GetMapping("/categories/edit/{id}")
    public String editCategory(@PathVariable Long id, Model model, RedirectAttributes redirectAttributes) {
        Optional<Category> categoryOpt = categoryRepository.findById(id);
        if (!categoryOpt.isPresent()) {
            redirectAttributes.addFlashAttribute("errorMessage", "Không tìm thấy danh mục!");
            return "redirect:/admin/categories";
        }

        model.addAttribute("category", categoryOpt.get());
        return "admin/categories";
    }

    @PostMapping("/categories/update/{id}")
    public String updateCategory(@PathVariable Long id, @ModelAttribute Category category,
            RedirectAttributes redirectAttributes) {
        Optional<Category> existingCategoryOpt = categoryRepository.findById(id);
        if (!existingCategoryOpt.isPresent()) {
            redirectAttributes.addFlashAttribute("errorMessage", "Không tìm thấy danh mục để cập nhật!");
            return "redirect:/admin/categories";
        }

        category.setId(id);
        categoryRepository.save(category);
        redirectAttributes.addFlashAttribute("successMessage", "Danh mục đã được cập nhật!");
        return "redirect:/admin/categories";
    }

    @GetMapping("/categories/delete/{id}")
    public String deleteCategory(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        if (categoryRepository.existsById(id)) {
            categoryRepository.deleteById(id);
            redirectAttributes.addFlashAttribute("successMessage", "Danh mục đã được xóa!");
        } else {
            redirectAttributes.addFlashAttribute("errorMessage", "Không tìm thấy danh mục!");
        }
        return "redirect:/admin/categories";
    }
    
 // ----------- QUẢN LÝ NGƯỜI DÙNG ----------- 

    // Hiển thị danh sách người dùng
    @GetMapping("/users")
    public String listUsers(Model model) {
        model.addAttribute("users", userRepository.findAll());
        model.addAttribute("user", new User()); // Form thêm người dùng mới
        return "admin/users";
    }

    // Thêm người dùng
    @PostMapping("/users/save")
    public String saveUser(@ModelAttribute User user, RedirectAttributes redirectAttributes) {
        userRepository.save(user);
        redirectAttributes.addFlashAttribute("successMessage", "Người dùng đã được lưu thành công!");
        return "redirect:/admin/users";
    }

    // Chỉnh sửa người dùng
    @GetMapping("/users/edit/{id}")
    public String editUser(@PathVariable Long id, Model model, RedirectAttributes redirectAttributes) {
        Optional<User> userOpt = userRepository.findById(id);
        if (!userOpt.isPresent()) {
            redirectAttributes.addFlashAttribute("errorMessage", "Không tìm thấy người dùng!");
            return "redirect:/admin/users";
        }

        model.addAttribute("user", userOpt.get());
        return "admin/users";
    }

    @PostMapping("/users/update/{id}")
    public String updateUser(@PathVariable Long id, @ModelAttribute User user, RedirectAttributes redirectAttributes) {
        Optional<User> existingUserOpt = userRepository.findById(id);
        if (!existingUserOpt.isPresent()) {
            redirectAttributes.addFlashAttribute("errorMessage", "Không tìm thấy người dùng để cập nhật!");
            return "redirect:/admin/users";
        }

        user.setId(id);
        userRepository.save(user);
        redirectAttributes.addFlashAttribute("successMessage", "Thông tin người dùng đã được cập nhật!");
        return "redirect:/admin/users";
    }

    // Xóa người dùng
    @GetMapping("/users/delete/{id}")
    public String deleteUser(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        if (userRepository.existsById(id)) {
            userRepository.deleteById(id);
            redirectAttributes.addFlashAttribute("successMessage", "Người dùng đã được xóa!");
        } else {
            redirectAttributes.addFlashAttribute("errorMessage", "Không tìm thấy người dùng!");
        }
        return "redirect:/admin/users";
    }
}
