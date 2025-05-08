package common;

/**
 * Response from authentication endpoints.
 * Includes the authenticated user's ID and JWT tokens.
 */
public record AuthResponse(
    String userId,
    String accessToken,
    String refreshToken,
    long expiresIn
) {
}
