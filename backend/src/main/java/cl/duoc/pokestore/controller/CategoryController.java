package cl.duoc.pokestore.controller;

import cl.duoc.pokestore.dto.CatalogDtos.CategoryRequest;
import cl.duoc.pokestore.dto.CatalogDtos.CategoryResponse;
import cl.duoc.pokestore.service.CategoryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirements;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/** GET es público; POST/PUT/DELETE solo ADMIN (regla en SecurityConfig). */
@Tag(name = "Categorías")
@RestController
@RequestMapping("/api/categories")
@RequiredArgsConstructor
public class CategoryController {

    private final CategoryService categoryService;

    @Operation(summary = "Listar tipos", description = "Ordenados alfabéticamente.")
    @SecurityRequirements // público
    @ApiResponse(responseCode = "200", description = "Listado")
    @GetMapping
    public List<CategoryResponse> findAll() {
        return categoryService.findAll();
    }

    @Operation(summary = "Obtener un tipo")
    @SecurityRequirements // público
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Encontrado"),
            @ApiResponse(responseCode = "404", description = "No existe")
    })
    @GetMapping("/{id}")
    public CategoryResponse findById(@Parameter(description = "ID del tipo", example = "1") @PathVariable Long id) {
        return categoryService.findById(id);
    }

    @Operation(summary = "Crear tipo [ADMIN]", description = "El nombre se guarda en minúsculas.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Creado"),
            @ApiResponse(responseCode = "400", description = "Datos inválidos"),
            @ApiResponse(responseCode = "403", description = "Requiere rol ADMIN"),
            @ApiResponse(responseCode = "409", description = "Ya existe un tipo con ese nombre")
    })
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public CategoryResponse create(@Valid @RequestBody CategoryRequest req) {
        return categoryService.create(req);
    }

    @Operation(summary = "Editar tipo [ADMIN]")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Actualizado"),
            @ApiResponse(responseCode = "400", description = "Datos inválidos"),
            @ApiResponse(responseCode = "403", description = "Requiere rol ADMIN"),
            @ApiResponse(responseCode = "404", description = "No existe"),
            @ApiResponse(responseCode = "409", description = "Ya existe un tipo con ese nombre")
    })
    @PutMapping("/{id}")
    public CategoryResponse update(@Parameter(description = "ID del tipo", example = "1") @PathVariable Long id,
                                   @Valid @RequestBody CategoryRequest req) {
        return categoryService.update(id, req);
    }

    @Operation(summary = "Eliminar tipo [ADMIN]", description = "No se puede eliminar si algún Pokémon lo usa.")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Eliminado"),
            @ApiResponse(responseCode = "403", description = "Requiere rol ADMIN"),
            @ApiResponse(responseCode = "404", description = "No existe"),
            @ApiResponse(responseCode = "409", description = "Hay Pokémon con este tipo")
    })
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@Parameter(description = "ID del tipo", example = "1") @PathVariable Long id) {
        categoryService.delete(id);
    }
}
