package com.hotel.user.controller;

import com.hotel.user.dto.LoginRequest;
import com.hotel.user.dto.LoginResponse;
import com.hotel.user.dto.UserResponse;
import com.hotel.user.model.User;
import com.hotel.user.security.JwtService;
import com.hotel.user.service.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/users")
public class UserController {

    private final UserService userService;
    private final JwtService jwtService;

    public UserController(UserService userService, JwtService jwtService) {
        this.userService = userService;
        this.jwtService = jwtService;
    }

    @PostMapping("/register")
    public ResponseEntity<UserResponse> register(@RequestBody User user) {

        User savedUser = userService.register(user);

        UserResponse response = new UserResponse(
                savedUser.getId(),
                savedUser.getName(),
                savedUser.getEmail()
        );

        return ResponseEntity.ok(response);
    }

    @GetMapping("/{email}")
    public ResponseEntity<UserResponse> getUserByEmail(@PathVariable String email) {

        return userService.findByEmail(email)
                .map(user -> new UserResponse(
                        user.getId(),
                        user.getName(),
                        user.getEmail()
                ))
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@RequestBody LoginRequest request) {

        boolean valid = userService.validateLogin(
                request.getEmail(),
                request.getPassword()
        );

        if (!valid) {
            return ResponseEntity.status(401)
                    .body(new LoginResponse(
                            null,
                            "Invalid email or password"
                    ));
        }

        String token = jwtService.generateToken(request.getEmail());

        return ResponseEntity.ok(
                new LoginResponse(
                        token,
                        "Login successful"
                )
        );
    }
}