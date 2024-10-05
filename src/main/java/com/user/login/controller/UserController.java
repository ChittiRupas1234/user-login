package com.user.login.controller;


import com.user.login.entity.User;
import com.user.login.excepitons.ResourceNotFoundException;
import com.user.login.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/user")
public class UserController {

    @Autowired
    UserService userService;

    @PostMapping("/register")
    public User insertData(@RequestBody User user) {
        userService.insertDetails(user);
        return user;
    }

    @GetMapping
    public ResponseEntity<List<User>> getAllUsers() {
        return ResponseEntity.ok(userService.getAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<User> getUserById(@PathVariable Long id) throws ResourceNotFoundException {
        User user = userService.getUserByidd(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found for this ID: " + id));
        return ResponseEntity.ok(user);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Map<String, String>> updateUser(@PathVariable Long id, @Valid @RequestBody User userDetails) throws ResourceNotFoundException {
        return ResponseEntity.ok(userService.updateUser(id, userDetails));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, String>> deleteUser(@PathVariable Long id) throws ResourceNotFoundException {
        return ResponseEntity.ok(userService.deleteStudentById(id));
    }
}