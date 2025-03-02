package com.assessment.speernotes.controller;

import com.assessment.speernotes.model.dto.UserAuthDto;
import com.assessment.speernotes.service.UsersService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@Slf4j
@RequestMapping("/api/auth")
@Tag(name = "Authentication End Points", description = "Endpoints used for user authentication")
public class AuthenticationController {
    private final UsersService usersService;

    public AuthenticationController(UsersService usersService) {
        this.usersService = usersService;
    }

    /**
     * This controller is used to sign up the new user
     *
     * @param user
     * @return ResponseEntity<String>
     */
    @PostMapping("/signup")
    @ResponseStatus(HttpStatus.OK)
    @Operation(summary = "Create the required user")
    public ResponseEntity<String> signup(@RequestBody @Valid UserAuthDto user) {
        log.info("POST /api/auth/signup {}", user.getEmail());
        if (usersService.isUserPresent(user.getEmail())) {
            return ResponseEntity.ok("User already exists!");
        }

        usersService.createUser(user);
        return ResponseEntity.ok("User registered successfully!");
    }

    /**
     * This controller is used to login the existing user
     *
     * @param user
     * @return ResponseEntity<String>
     */
    @PostMapping("/login")
    @ResponseStatus(HttpStatus.OK)
    @Operation(summary = "Successfully login the valid user")
    public ResponseEntity<String> login(@RequestBody @Valid UserAuthDto user) {
        log.info("POST /api/auth/login {}", user.getEmail());
        String userJWT = usersService.getUserJWT(user);

        if (userJWT.isEmpty()) {
            return ResponseEntity.ok("Invalid credentials");
        }

        return ResponseEntity.ok(userJWT);
    }
}
