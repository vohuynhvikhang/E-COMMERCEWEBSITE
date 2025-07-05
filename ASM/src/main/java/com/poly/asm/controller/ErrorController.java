package com.poly.asm.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
public class ErrorController implements org.springframework.boot.web.servlet.error.ErrorController {

    private static final Logger logger = LoggerFactory.getLogger(ErrorController.class);

    @RequestMapping("/error")
    public String handleError() {
        logger.warn("Error page accessed");
        return "redirect:/login?error=" + encodeErrorMessage("Đã xảy ra lỗi, vui lòng thử lại");
    }

    private String encodeErrorMessage(String message) {
        try {
            return java.net.URLEncoder.encode(message, java.nio.charset.StandardCharsets.UTF_8.toString());
        } catch (Exception e) {
            logger.error("Failed to encode error message: {}", e.getMessage());
            return "Error_encoding_message";
        }
    }
}