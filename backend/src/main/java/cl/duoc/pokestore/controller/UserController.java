package cl.duoc.pokestore.controller;

import cl.duoc.pokestore.dto.UserDtos.RoleResponse;
import cl.duoc.pokestore.dto.UserDtos.UserRequest;
import cl.duoc.pokestore.dto.UserDtos.UserResponse;
import cl.duoc.pokestore.security.AuthUser;
import cl.duoc.pokestore.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/** Solo ADMIN (regla en SecurityConfig). */
@Tag(name = "Usuarios")
@ApiResponse(responseCode = "403", description = "Requiere rol ADMIN")
@RestController
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @Operation(summary = "Listar usuarios [ADMIN]")
    @ApiResponse(responseCode = "200", description = "Listado")
    @GetMapping("/api/users")
    public List<UserResponse> findAll() {
        return userService.findAll();
    }

    @Operation(summary = "Obtener un usuario [ADMIN]")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Encontrado"),
            @ApiResponse(responseCode = "404", description = "No existe")
    })
    @GetMapping("/api/users/{id}")
    public UserResponse findById(@Parameter(description = "ID del usuario", example = "2") @PathVariable Long id) {
        return userService.findById(id);
    }

    @Operation(summary = "Crear usuario [ADMIN]", description = "La contraseña es obligatoria al crear.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Creado"),
            @ApiResponse(responseCode = "400", description = "Datos inválidos, falta contraseña o rol inexistente"),
            @ApiResponse(responseCode = "409", description = "El email ya está registrado")
    })
    @PostMapping("/api/users")
    @ResponseStatus(HttpStatus.CREATED)
    public UserResponse create(@Valid @RequestBody UserRequest req) {
        return userService.create(req);
    }

    @Operation(summary = "Editar usuario [ADMIN]",
            description = """
                    Si `password` viene null u omitido, se mantiene la actual. \
                    No puedes quitarte el rol ADMIN a ti mismo ni degradar al último administrador.""")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Actualizado"),
            @ApiResponse(responseCode = "400", description = "Datos inválidos o regla de administradores"),
            @ApiResponse(responseCode = "404", description = "No existe"),
            @ApiResponse(responseCode = "409", description = "El email ya está registrado")
    })
    @PutMapping("/api/users/{id}")
    public UserResponse update(@Parameter(description = "ID del usuario", example = "2") @PathVariable Long id,
                               @Valid @RequestBody UserRequest req,
                               @Parameter(hidden = true) @AuthenticationPrincipal AuthUser me) {
        return userService.update(id, req, me);
    }

    @Operation(summary = "Eliminar usuario [ADMIN]",
            description = "No puedes eliminarte a ti mismo, ni al último admin, ni a usuarios con compras.")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Eliminado"),
            @ApiResponse(responseCode = "400", description = "Regla de administradores"),
            @ApiResponse(responseCode = "404", description = "No existe"),
            @ApiResponse(responseCode = "409", description = "Tiene compras registradas")
    })
    @DeleteMapping("/api/users/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@Parameter(description = "ID del usuario", example = "2") @PathVariable Long id,
                       @Parameter(hidden = true) @AuthenticationPrincipal AuthUser me) {
        userService.delete(id, me);
    }

    @Operation(summary = "Listar roles [ADMIN]")
    @ApiResponse(responseCode = "200", description = "Listado")
    @GetMapping("/api/roles")
    public List<RoleResponse> roles() {
        return userService.roles();
    }
}
