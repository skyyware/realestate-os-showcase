package com.skyyware.realestate.identity;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
public class AuthController {
    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/register")
    AuthService.RegistrationResult register(@Valid @RequestBody RegisterRequest request) {
        return authService.register(request.email(), request.fullName(), request.organizationName());
    }

    @GetMapping("/registration/{token}")
    AuthService.RegistrationPreview preview(@PathVariable String token) {
        return authService.previewRegistration(token);
    }

    @PostMapping("/password")
    AuthService.AuthSession setPassword(@Valid @RequestBody SetPasswordRequest request) {
        return authService.setPassword(request.token(), request.password());
    }

    @PostMapping("/password-reset")
    AuthService.RegistrationResult requestPasswordReset(@Valid @RequestBody PasswordResetRequest request) {
        return authService.requestPasswordReset(request.email());
    }

    @PostMapping("/login")
    AuthService.AuthSession login(@Valid @RequestBody LoginRequest request) {
        return authService.login(request.email(), request.password());
    }

    public record RegisterRequest(
            @Email @NotBlank String email,
            @NotBlank @Size(min = 2, max = 180) String fullName,
            @NotBlank @Size(min = 2, max = 180) String organizationName
    ) {
    }

    public record SetPasswordRequest(
            @NotBlank String token,
            @NotBlank @Size(min = 10, max = 128) String password
    ) {
    }

    public record PasswordResetRequest(
            @Email @NotBlank String email
    ) {
    }

    public record LoginRequest(
            @Email @NotBlank String email,
            @NotBlank String password
    ) {
    }
}
