package com.example.cloudfilestorage.Controller;

import com.example.cloudfilestorage.DTO.ErrorResponse;
import com.example.cloudfilestorage.DTO.UserDTO;
import com.example.cloudfilestorage.DTO.UserResponseDTO;
import com.example.cloudfilestorage.Service.ResourceService;
import com.example.cloudfilestorage.Service.StorageService;
import com.example.cloudfilestorage.Service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
@Tag(name = "Authenticate", description = "Авторизація,реєстрація і вихід з сесії ")
public class AuthController {

    private final UserService userService;
    private final AuthenticationManager authenticationManager;

    public AuthController(UserService userService, AuthenticationManager authenticationManager) {
        this.userService = userService;
        this.authenticationManager = authenticationManager;
    }

    @Operation(summary = "Вихід з сесії")
    @ApiResponses({
            @ApiResponse(responseCode = "401", description = "Користувач не авторизований",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "500", description = "Помилка сервера",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @PostMapping("/sign-out")
    public ResponseEntity<Void> logout(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        if (session != null) {
            session.invalidate();
        }
        SecurityContextHolder.clearContext();

        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Вхід для користувача")
    @ApiResponses({
            @ApiResponse(responseCode = "400", description = "Закоротке імя користувача",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "401", description = "Такого користувача не існує ",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "500", description = "Помилка сервера",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @io.swagger.v3.oas.annotations.parameters.RequestBody(
            description = "Дані для входу",content = @Content(schema = @Schema(implementation = UserDTO.class)))
    @PostMapping("/sign-in")
    public ResponseEntity<UserResponseDTO> login(@RequestBody UserDTO userDTO, HttpServletRequest request) {
        if (userDTO.username().length() < 5) return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        authenticateAndSaveSession(userDTO, request);

        return ResponseEntity.ok(new UserResponseDTO(userDTO.username()));
    }

    @Operation(summary = "Реєстрація для користувача")
    @ApiResponses({
            @ApiResponse(responseCode = "400", description = "Закоротке імя користувача",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "409", description = "Такий користувач вже існує ",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "500", description = "Помилка сервера",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @io.swagger.v3.oas.annotations.parameters.RequestBody(
            description = "Дані для реєстрації",content = @Content(schema = @Schema(implementation = UserDTO.class)))
    @PostMapping("/sign-up")
    public ResponseEntity<UserResponseDTO> register(@RequestBody UserDTO userDTO, HttpServletRequest request) {
        if (userDTO.username().length() < 5) return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
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
