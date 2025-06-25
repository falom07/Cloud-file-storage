package com.example.cloudfilestorage.Controller;

import com.example.cloudfilestorage.Service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class UserController {

    private UserService userService;

    @Autowired
    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/name")
    public String name() {
        userService.findById(1L);
        return "name";
    }
}
