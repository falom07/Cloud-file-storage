package com.example.cloudfilestorage.Controller;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/user")
public class UserController {
    @GetMapping
    public ResponseEntity<Map<String, String>> getUser(Authentication auth) {
        String username = auth.getName();

        return ResponseEntity.ok(Map.of("username",username));
    }
}
