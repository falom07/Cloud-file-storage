package com.example.cloudfilestorage.Controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class LogInController {

    @GetMapping("/login")
    public String login() {
        return "login";
    }

//    @GetMapping("")

}
