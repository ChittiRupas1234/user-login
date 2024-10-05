package com.user.login.service;

import com.user.login.dto.UpdateUserRequest;
import com.user.login.entity.User;
import com.user.login.models.AuthenticationRequest;
import com.user.login.repository.UserRepository;
import com.user.login.utils.JwtUtil;
import com.user.login.excepitons.ResourceNotFoundException;
import com.user.login.excepitons.UserDefinedExceptions;
import com.user.login.excepitons.UsernameAlreadyExistsException;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
public class UserService {

    @Autowired
    UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private UserDetailsService userDetailsService;

    public Map<String, String> registerUser(User user) {
        String emailValidationMessage = validateEmail(user.getUsername());
        if (!emailValidationMessage.isEmpty()) {
            throw new UserDefinedExceptions(emailValidationMessage);
        }

        String passwordValidationMessage = validatePassword(user.getPassword());
        if (!passwordValidationMessage.isEmpty()) {
            throw new UserDefinedExceptions(passwordValidationMessage);
        }

        Optional<User> existingUser = findByUsername(user.getUsername());
        if (existingUser.isPresent()) {
            throw new UsernameAlreadyExistsException("Username already exists.");
        }

        user.setPassword(passwordEncoder.encode(user.getPassword()));
        userRepository.save(user);

        Map<String, String> response = new HashMap<>();
        response.put("message", "User registered successfully.");
        return response;
    }

    private String validateEmail(String email) {
        if (!(email.contains("@") && email.endsWith(".com"))) {
            throw new UserDefinedExceptions("Email must contain '@' and end with '.com'.");
        }
        return "";
    }

    private String validatePassword(String password) {
        String passwordPattern = "^(?=.*[A-Z])(?=.*[a-z])(?=.*\\d)(?=.*[@$!%*?&])[A-Za-z\\d@$!%*?&]{6,16}$";
        if (!password.matches(passwordPattern)) {
            throw new UserDefinedExceptions("Password must be 6-16 characters long, contain at least one uppercase letter, one lowercase letter, one number, and one special character (@, $, !, %, *, ?, &).");
        }
        return "";
    }

    public Optional<User> findByUsername(String username) {
        return userRepository.findByUsername(username);
    }

    public Map<String, String> authenticateAndGenerateToken(AuthenticationRequest authRequest) throws Exception {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(authRequest.getUsername(), authRequest.getPassword())
        );
        SecurityContextHolder.getContext().setAuthentication(authentication);

        UserDetails userDetails = userDetailsService.loadUserByUsername(authRequest.getUsername());
        String token = jwtUtil.generateToken(userDetails.getUsername());

        // Create response
        Map<String, String> response = new HashMap<>();
        response.put("jwtToken", token);
        return response;
    }

    public boolean validateJwtToken(HttpServletRequest request) {
        final String authorizationHeader = request.getHeader("Authorization");

        String token = null;
        String username = null;

        if (authorizationHeader != null && authorizationHeader.startsWith("Bearer ")) {
            token = authorizationHeader.substring(7);
            try {
                username = jwtUtil.extractUsername(token);
            } catch (Exception e) {
                return false;
            }
        } else {
            return false;
        }

        if (username != null && token != null) {
            UserDetails userDetails = userDetailsService.loadUserByUsername(username);
            return jwtUtil.validateToken(token, userDetails.getUsername());
        }

        return false;
    }

    public Map<String, String> updateUserDetails(UpdateUserRequest updateUserRequest) {
        if (updateUserRequest.getUserId() == null) {
            throw new UsernameAlreadyExistsException("User ID must not be null");
        }

        User currentUser = getUserById(updateUserRequest.getUserId())
                .orElseThrow(() -> new UsernameAlreadyExistsException("User not found"));

        String emailValidationMessage = validateEmail(updateUserRequest.getUsername());
        if (!emailValidationMessage.isEmpty()) {
            throw new UserDefinedExceptions(emailValidationMessage);
        }

        if (updateUserRequest.getPassword() != null && !updateUserRequest.getPassword().isEmpty()) {
            String passwordValidationMessage = validatePassword(updateUserRequest.getPassword());
            if (!passwordValidationMessage.isEmpty()) {
                throw new UserDefinedExceptions(passwordValidationMessage);
            }
            currentUser.setPassword(passwordEncoder.encode(updateUserRequest.getPassword()));
        }

        Optional<User> existingUser = findByUsername(updateUserRequest.getUsername());
        if (existingUser.isPresent() && !existingUser.get().getId().equals(updateUserRequest.getUserId())) {
            throw new UsernameAlreadyExistsException("Username already exists.");
        }

        if (!currentUser.getUsername().equals(updateUserRequest.getUsername())) {
            currentUser.setUsername(updateUserRequest.getUsername());
        }

        if (!currentUser.getName().equals(updateUserRequest.getName())) {
            currentUser.setName(updateUserRequest.getName());
        }

        userRepository.save(currentUser);

        Map<String, String> response = new HashMap<>();
        response.put("message", "User details updated successfully.");
        return response;
    }

    public Optional<User> getUserById(Long uId) {
        return userRepository.findById(uId);
    }

    public void insertDetails(User user) {
        user.setPassword(passwordEncoder.encode(user.getPassword())); // Ensure password is encoded
        userRepository.save(user);
    }

    public List<User> getAll() {
        return userRepository.findAll();
    }

    public Optional<User> getUserByidd(Long id) {
        return userRepository.findById(id);
    }

    public Map<String, String> updateUser(Long id, User userDetails) throws ResourceNotFoundException {
        User user = getUserById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found for this ID: " + id));

        user.setName(userDetails.getName());
        user.setUsername(userDetails.getUsername());

        if (userDetails.getPassword() != null && !userDetails.getPassword().isEmpty()) {
            user.setPassword(passwordEncoder.encode(userDetails.getPassword()));
        }

        userRepository.save(user);

        Map<String, String> response = new HashMap<>();
        response.put("message", "User updated successfully.");
        return response;
    }

    public Map<String, String> deleteStudentById(Long id) throws ResourceNotFoundException {
        User user = getUserById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found for this ID: " + id));

        userRepository.delete(user);

        Map<String, String> response = new HashMap<>();
        response.put("message", "User deleted successfully.");
        return response;
    }
}
