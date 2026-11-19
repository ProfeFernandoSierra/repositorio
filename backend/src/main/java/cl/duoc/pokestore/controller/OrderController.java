package cl.duoc.pokestore.controller;

import cl.duoc.pokestore.dto.OrderDtos.OrderRequest;
import cl.duoc.pokestore.dto.OrderDtos.OrderResponse;
import cl.duoc.pokestore.security.AuthUser;
import cl.duoc.pokestore.service.OrderService;
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

@Tag(name = "Órdenes")
@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;

    /** Cualquier usuario logueado. El usuario sale del token, no del body. */
    @Operation(summary = "Comprar",
            description = """
                    Crea una orden para el usuario del token. En una sola transacción valida el stock, \
                    lo descuenta y calcula el total con los precios de la BD. Si un producto viene \
                    repetido, se suman sus cantidades.""")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Orden creada"),
            @ApiResponse(responseCode = "400", description = "Datos inválidos o más de 99 unidades de un producto"),
            @ApiResponse(responseCode = "404", description = "Algún producto no existe"),
            @ApiResponse(responseCode = "409", description = "Stock insuficiente")
    })
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public OrderResponse create(@Valid @RequestBody OrderRequest req,
                                @Parameter(hidden = true) @AuthenticationPrincipal AuthUser me) {
        return orderService.create(me, req);
    }

    @Operation(summary = "Mis compras", description = "Órdenes del usuario del token, de la más reciente a la más antigua.")
    @ApiResponse(responseCode = "200", description = "Listado")
    @GetMapping("/me")
    public List<OrderResponse> mine(@Parameter(hidden = true) @AuthenticationPrincipal AuthUser me) {
        return orderService.mine(me);
    }

    /** Solo ADMIN (regla en SecurityConfig). */
    @Operation(summary = "Todas las órdenes [ADMIN]")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Listado"),
            @ApiResponse(responseCode = "403", description = "Requiere rol ADMIN")
    })
    @GetMapping
    public List<OrderResponse> findAll() {
        return orderService.findAll();
    }
}
