package com.skyyware.realestate.security;

import java.util.UUID;

public record JwtPrincipal(UUID userId, String email, String name, String role) {
}
