package cl.duoc.pokestore.dto;

import cl.duoc.pokestore.entity.Role;
import cl.duoc.pokestore.entity.User;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public final class UserDtos {

    private UserDtos() {
    }

    /** Respuesta SIN password (ni siquiera el hash sale de la API). */
    @Schema(description = "Usuario (nunca incluye la contraseña)")
    public record UserResponse(
            @Schema(example = "2") Long id,
            @Schema(example = "Ash Ketchum") String name,
            @Schema(example = "ash@pokemon.com") String email,
            @Schema(example = "USER", allowableValues = {"ADMIN", "USER"}) String role) {
        public static UserResponse from(User u) {
            return new UserResponse(u.getId(), u.getName(), u.getEmail(), u.getRole().getName());
        }
    }

    /** Usado por el admin. En edición, password vacío/null = no cambiarla. */
    @Schema(description = "Datos de usuario para el mantenedor (ADMIN)")
    public record UserRequest(
            @Schema(example = "Brock") @NotBlank @Size(max = 80) String name,
            @Schema(example = "brock@pokemon.com") @NotBlank @Email @Size(max = 120) String email,
            @Schema(description = "Obligatoria al crear. Al editar, omitir o null para no cambiarla",
                    example = "Onix12345", minLength = 8, maxLength = 72, nullable = true)
            @Size(min = 8, max = 72, message = "debe tener entre 8 y 72 caracteres") String password,
            @Schema(example = "USER", allowableValues = {"ADMIN", "USER"}) @NotBlank String role) {
    }

    @Schema(description = "Rol del sistema")
    public record RoleResponse(
            @Schema(example = "1") Long id,
            @Schema(example = "ADMIN") String name) {
        public static RoleResponse from(Role r) {
            return new RoleResponse(r.getId(), r.getName());
        }
    }
}
