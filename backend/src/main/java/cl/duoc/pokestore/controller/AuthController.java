package cl.duoc.pokestore.controller;

import cl.duoc.pokestore.dto.AuthDtos.AuthResponse;
import cl.duoc.pokestore.dto.AuthDtos.LoginRequest;
import cl.duoc.pokestore.dto.AuthDtos.RegisterRequest;
import cl.duoc.pokestore.dto.UserDtos.UserResponse;
import cl.duoc.pokestore.security.AuthUser;
import cl.duoc.pokestore.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirements;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Auth")
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @Operation(summary = "Iniciar sesión", description = "Devuelve un JWT válido por 120 minutos (configurable).")
    @SecurityRequirements // público
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Login correcto"),
            @ApiResponse(responseCode = "400", description = "Datos inválidos"),
            @ApiResponse(responseCode = "401", description = "Credenciales inválidas")
    })
    @PostMapping("/login")
    public AuthResponse login(@Valid @RequestBody LoginRequest req) {
        return authService.login(req);
    }

    @Operation(summary = "Registrarse", description = "Crea una cuenta con rol USER y devuelve su JWT.")
    @SecurityRequirements // público
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Cuenta creada"),
            @ApiResponse(responseCode = "400", description = "Datos inválidos"),
            @ApiResponse(responseCode = "409", description = "El email ya está registrado")
    })
    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    public AuthResponse register(@Valid @RequestBody RegisterRequest req) {
        return authService.register(req);
    }

    @Operation(summary = "Usuario actual", description = "Datos del dueño del token (rol incluido, leído desde la BD).")
    @ApiResponse(responseCode = "200", description = "Usuario autenticado")
    @GetMapping("/me")
    public UserResponse me(@Parameter(hidden = true) @AuthenticationPrincipal AuthUser me) {
        return authService.me(me);
    }
}
