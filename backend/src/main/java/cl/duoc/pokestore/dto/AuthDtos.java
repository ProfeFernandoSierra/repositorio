package cl.duoc.pokestore.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public final class AuthDtos {

    private AuthDtos() {
    }

    @Schema(description = "Credenciales de acceso")
    public record LoginRequest(
            @Schema(example = "admin@pokestore.cl") @NotBlank @Email @Size(max = 120) String email,
            @Schema(example = "Admin1234") @NotBlank @Size(max = 72) String password) {
    }

    /** El registro público SIEMPRE crea usuarios con rol USER (no se acepta "role" desde el cliente). */
    @Schema(description = "Datos para crear una cuenta. El rol siempre será USER")
    public record RegisterRequest(
            @Schema(example = "Misty") @NotBlank @Size(max = 80) String name,
            @Schema(example = "misty@pokemon.com") @NotBlank @Email @Size(max = 120) String email,
            @Schema(example = "Staryu123", minLength = 8, maxLength = 72)
            @NotBlank @Size(min = 8, max = 72, message = "debe tener entre 8 y 72 caracteres") String password) {
    }

    @Schema(description = "Token JWT y datos del usuario autenticado")
    public record AuthResponse(
            @Schema(description = "JWT para el header Authorization: Bearer <token>",
                    example = "eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiIxIiwicm9sZSI6IkFETUlOIn0.firma")
            String token,
            UserDtos.UserResponse user) {
    }
}
