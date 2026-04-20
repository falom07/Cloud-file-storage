package com.example.cloudfilestorage.Controller;

import com.example.cloudfilestorage.DTO.UserDTO;
import com.example.cloudfilestorage.DTO.UserResponseDTO;
import com.example.cloudfilestorage.Entity.User;
import com.example.cloudfilestorage.Service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final UserService userService;
    private final AuthenticationManager authenticationManager;

    public AuthController(UserService userService, AuthenticationManager authenticationManager) {
        this.userService = userService;
        this.authenticationManager = authenticationManager;
    }

    @PostMapping("/sign-out")
    public ResponseEntity<Void> logout(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        if(session != null){
            session.invalidate();
        }
        SecurityContextHolder.clearContext();

        return ResponseEntity.noContent().build();
    }

    @PostMapping("/sign-in")
    public ResponseEntity<UserResponseDTO> login(@RequestBody UserDTO userDTO ,HttpServletRequest request){
        authenticateAndSaveSession(userDTO,request);

        return ResponseEntity.ok(new UserResponseDTO(userDTO.username()));
    }

    @PostMapping("/sign-up")
    public ResponseEntity<UserResponseDTO> register(@RequestBody UserDTO userDTO, HttpServletRequest request) {
        userService.createUser(userDTO);
        authenticateAndSaveSession(userDTO, request);

        return ResponseEntity.status(HttpStatus.CREATED).body(new UserResponseDTO(userDTO.username()));
    }

    private void authenticateAndSaveSession(UserDTO userDTO, HttpServletRequest request) {
        UsernamePasswordAuthenticationToken authToken =
                new UsernamePasswordAuthenticationToken(
                        userDTO.username(),
                        userDTO.password()
                );

        Authentication authentication = authenticationManager.authenticate(authToken);

        SecurityContext context = SecurityContextHolder.createEmptyContext();
        context.setAuthentication(authentication);
        SecurityContextHolder.setContext(context);

        HttpSession session = request.getSession(true);
        session.setAttribute(
                HttpSessionSecurityContextRepository.SPRING_SECURITY_CONTEXT_KEY,
                context
        );
    }
}
