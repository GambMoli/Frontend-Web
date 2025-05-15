package com.example.demo.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
public class HomeController {

    @GetMapping("/")
    public String home(Model model) {
        model.addAttribute("mensaje", "¡Hola desde Spring Boot con JSP!");
        return "home";
    }
    @GetMapping("/plain")
    @ResponseBody
    public String plainText() {
        return "Este texto debe aparecer sin problemas";
    }
}