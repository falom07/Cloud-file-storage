package com.example.cloudfilestorage.Controller;

import com.example.cloudfilestorage.DTO.UserDTO;
import com.example.cloudfilestorage.Service.MyUserDetailsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
public class HomePageController {

    private MyUserDetailsService myUserDetailsService;

    @Autowired
    public HomePageController(MyUserDetailsService myUserDetailsService) {
        this.myUserDetailsService = myUserDetailsService;
    }

    @GetMapping
    public String getFirstPage() {
        return "home";
    }

    @GetMapping("/home")
    public String name() {
        return "home";
    }

    @PostMapping("/sign-in")
    public ResponseEntity<String> signIn(@RequestBody UserDTO userDto) {
        System.out.println(userDto.toString());
        if(userDto.username().equals(userDto.password())) {
            return ResponseEntity.status(HttpStatus.NON_AUTHORITATIVE_INFORMATION).body(userDto.toString());
        }
        return ResponseEntity.ok("okay");
    }
}
