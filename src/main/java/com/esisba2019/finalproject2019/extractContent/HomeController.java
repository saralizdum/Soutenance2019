package com.esisba2019.finalproject2019.extractContent;


import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class HomeController {

    @GetMapping("/")
    public String root() {
        return "index";
    }

    @GetMapping("/classification")
    public String userIndex() {
        return "/classification";
    }

}