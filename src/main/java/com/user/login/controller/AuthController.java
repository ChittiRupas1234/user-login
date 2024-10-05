package com.user.login.controller;

import com.user.login.dto.UpdateUserRequest;
import com.user.login.entity.User;
import com.user.login.models.AuthenticationRequest;
import com.user.login.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Map;


@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Autowired
    private UserService userService;

    @PostMapping("/register")
    public Map<String, String> register(@RequestBody User user) {
        return userService.registerUser(user);
    }

    @PostMapping("/login")
    public Map<String, String> login(@RequestBody AuthenticationRequest authRequest) throws Exception {
        return userService.authenticateAndGenerateToken(authRequest);
    }

    @RequestMapping(value = "/validate", method = {RequestMethod.GET, RequestMethod.POST, RequestMethod.PUT, RequestMethod.DELETE})
    public boolean validateToken(HttpServletRequest request) {
        return userService.validateJwtToken(request);
    }

    @PostMapping("/update")
    public Map<String, String> updateUserDetails(@RequestHeader("Authorization") String token, @RequestBody UpdateUserRequest updateUserRequest) {
        return userService.updateUserDetails(updateUserRequest);
    }
}

