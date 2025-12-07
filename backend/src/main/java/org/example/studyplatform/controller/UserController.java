package org.example.studyplatform.controller;

import jakarta.validation.Valid;
import org.example.studyplatform.dto.RegisterRequest;
import org.example.studyplatform.dto.LoginRequest;
import org.example.studyplatform.dto.UserResponse;
import org.example.studyplatform.entity.User;
import org.example.studyplatform.service.UserService;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/users")
@CrossOrigin
public class UserController {

    private final UserService service;

    public UserController(UserService service) {
        this.service = service;
    }

    @PostMapping("/register")
    public UserResponse register(@Valid @RequestBody RegisterRequest request) {
        User user = service.register(
                request.getName(),
                request.getEmail(),
                request.getPassword()
        );
        return new UserResponse(user.getId(), user.getName(), user.getEmail());
    }

    @PostMapping("/login")
    public UserResponse login(@Valid @RequestBody LoginRequest request) {
        User user = service.login(
                request.getName(),
                request.getPassword()
        );
        return new UserResponse(user.getId(), user.getName(), user.getEmail());
    }

    @GetMapping
    public List<UserResponse> getAllUsers() {
        return service.getAllUsers()
                .stream()
                .map(u -> new UserResponse(u.getId(), u.getName(), u.getEmail()))
                .toList();
    }

    @GetMapping("/{id}")
    public UserResponse getUser(@PathVariable("id") Long id) {
        User user = service.getUser(id);
        return new UserResponse(user.getId(), user.getName(), user.getEmail());
    }

    @PostMapping("/{id}/change-password")
    public UserResponse changePassword(@PathVariable("id") Long id, @RequestBody Map<String, String> body) {
        String newPassword = body.get("newPassword");
        User user = service.changePassword(id, newPassword);
        return new UserResponse(user.getId(), user.getName(), user.getEmail());
    }

    @PostMapping("/{id}/change-email")
    public UserResponse changeEmail(@PathVariable("id") Long id, @RequestBody Map<String, String> body) {
        String newEmail = body.get("newEmail");
        User user = service.changeEmail(id, newEmail);
        return new UserResponse(user.getId(), user.getName(), user.getEmail());
    }
}
