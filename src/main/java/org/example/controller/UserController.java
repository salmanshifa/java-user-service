package org.example.controller;

import jakarta.validation.Valid;
import org.example.controller.dto.ApiResponse;
import org.example.controller.dto.CreateUserRequest;
import org.example.controller.dto.CreateUserResponse;
import org.example.controller.dto.LoginRequest;
import org.example.controller.dto.LoginResponse;
import org.example.controller.dto.UpdateUserRequest;
import org.example.model.User;
import org.example.service.UserService;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/users")
public class UserController {
    private static final Logger log = LoggerFactory.getLogger(UserController.class);
    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping
    public ApiResponse<List<User>> listUsers() {
        List<User> users = userService.findAll();
        log.info("Reading all users. Total users returned: {}", users.size());
        return ApiResponse.success("Users retrieved", users);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<User>> getUser(@PathVariable Long id) {
        log.info("Reading user with id: {}", id);
        return userService.findById(id)
                .map(user -> {
                    log.info("User found for id: {}", id);
                    return ResponseEntity.ok(ApiResponse.success("User retrieved", user));
                })
                .orElseGet(() -> {
                    log.warn("User not found for id: {}", id);
                    return ResponseEntity.status(HttpStatus.NOT_FOUND)
                            .body(ApiResponse.error("User not found", null));
                });
    }

    @GetMapping("/profile")
    public ResponseEntity<ApiResponse<User>> getProfile(Authentication authentication) {
        return userService.findByUsername(authentication.getName())
                .map(user -> ResponseEntity.ok(ApiResponse.success("Profile retrieved", user)))
                .orElseGet(() -> ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(ApiResponse.error("User not found", null)));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<CreateUserResponse>> createUser(@Valid @RequestBody CreateUserRequest request) {
        User created = userService.create(request.username(), request.email(), request.mobileNumber(), request.password(), true, request.role());
        String token = userService.generateJwtToken(created);
        CreateUserResponse response = new CreateUserResponse(created, token);
        return ResponseEntity.created(URI.create("/users/" + created.id()))
                .body(ApiResponse.success("User created", response));
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ApiResponse<Void>> handleIllegalArgument(IllegalArgumentException ex) {
        return ResponseEntity.badRequest().body(ApiResponse.error(ex.getMessage(), null));
    }

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<LoginResponse>> login(@Valid @RequestBody LoginRequest request) {
        log.info("Login attempt for username={}", request.username());
        return userService.authenticate(request.username(), request.password())
                .map(user -> {
                    log.info("Login successful for username={}", request.username());
                    String token = userService.generateJwtToken(user);
                    return ResponseEntity.ok(ApiResponse.success(LoginResponse.of(token, "Login successful", user)));
                })
                .orElseGet(() -> {
                    log.warn("Login failed for username={}", request.username());
                    return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                            .body(ApiResponse.error("Invalid credentials", null));
                });
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<User>> updateUser(@PathVariable Long id, @Valid @RequestBody UpdateUserRequest request) {
        return userService.update(id, request.username(), request.email(), request.mobileNumber())
                .map(user -> ResponseEntity.ok(ApiResponse.success("User updated", user)))
                .orElseGet(() -> ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(ApiResponse.error("User not found", null)));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteUser(@PathVariable Long id) {
        return userService.delete(id)
                ? ResponseEntity.ok(ApiResponse.success("User deleted"))
                : ResponseEntity.status(HttpStatus.NOT_FOUND).body(ApiResponse.error("User not found", null));
    }

}
