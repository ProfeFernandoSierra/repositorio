package cl.duoc.pokestore.controller;

import cl.duoc.pokestore.dto.CatalogDtos.ProductRequest;
import cl.duoc.pokestore.dto.CatalogDtos.ProductResponse;
import cl.duoc.pokestore.service.ProductService;
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
@Tag(name = "Productos")
@RestController
@RequestMapping("/api/products")
@RequiredArgsConstructor
public class ProductController {

    private final ProductService productService;

    @Operation(summary = "Listar Pokémon", description = "Ordenados por número de Pokédex.")
    @SecurityRequirements // público
    @ApiResponse(responseCode = "200", description = "Listado")
    @GetMapping
    public List<ProductResponse> findAll() {
        return productService.findAll();
    }

    @Operation(summary = "Obtener un Pokémon")
    @SecurityRequirements // público
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Encontrado"),
            @ApiResponse(responseCode = "404", description = "No existe")
    })
    @GetMapping("/{id}")
    public ProductResponse findById(@Parameter(description = "ID del producto", example = "1") @PathVariable Long id) {
        return productService.findById(id);
    }

    @Operation(summary = "Crear Pokémon [ADMIN]")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Creado"),
            @ApiResponse(responseCode = "400", description = "Datos inválidos o tipo inexistente"),
            @ApiResponse(responseCode = "403", description = "Requiere rol ADMIN"),
            @ApiResponse(responseCode = "409", description = "Ya existe un Pokémon con ese nombre")
    })
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ProductResponse create(@Valid @RequestBody ProductRequest req) {
        return productService.create(req);
    }

    @Operation(summary = "Editar Pokémon [ADMIN]")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Actualizado"),
            @ApiResponse(responseCode = "400", description = "Datos inválidos o tipo inexistente"),
            @ApiResponse(responseCode = "403", description = "Requiere rol ADMIN"),
            @ApiResponse(responseCode = "404", description = "No existe"),
            @ApiResponse(responseCode = "409", description = "Ya existe un Pokémon con ese nombre")
    })
    @PutMapping("/{id}")
    public ProductResponse update(@Parameter(description = "ID del producto", example = "1") @PathVariable Long id,
                                  @Valid @RequestBody ProductRequest req) {
        return productService.update(id, req);
    }

    @Operation(summary = "Eliminar Pokémon [ADMIN]",
            description = "No se puede eliminar si tiene ventas: en ese caso dejar su stock en 0.")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Eliminado"),
            @ApiResponse(responseCode = "403", description = "Requiere rol ADMIN"),
            @ApiResponse(responseCode = "404", description = "No existe"),
            @ApiResponse(responseCode = "409", description = "Tiene ventas registradas")
    })
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@Parameter(description = "ID del producto", example = "1") @PathVariable Long id) {
        productService.delete(id);
    }
}
