package cl.duoc.pokestore.dto;

import cl.duoc.pokestore.entity.OrderItem;
import cl.duoc.pokestore.entity.PurchaseOrder;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;

import java.time.Instant;
import java.util.List;

public final class OrderDtos {

    private OrderDtos() {
    }

    /** El cliente solo envía QUÉ compra y CUÁNTO. El precio y el total los pone el servidor. */
    @Schema(description = "Compra: solo productos y cantidades. Precio y total los calcula el servidor")
    public record OrderRequest(@NotEmpty @Size(max = 50) List<@Valid OrderItemRequest> items) {
    }

    @Schema(description = "Línea de compra")
    public record OrderItemRequest(
            @Schema(example = "4") @NotNull Long productId,
            @Schema(example = "2", minimum = "1", maximum = "99") @NotNull @Min(1) @Max(99) Integer quantity) {
    }

    @Schema(description = "Línea de una orden (precio congelado al momento de comprar)")
    public record OrderItemResponse(
            @Schema(example = "4") Long productId,
            @Schema(example = "Pikachu") String productName,
            @Schema(example = "7990") Integer unitPrice,
            @Schema(example = "2") Integer quantity,
            @Schema(example = "15980") Integer subtotal) {
        public static OrderItemResponse from(OrderItem i) {
            return new OrderItemResponse(i.getProduct().getId(), i.getProductName(), i.getUnitPrice(),
                    i.getQuantity(), i.getUnitPrice() * i.getQuantity());
        }
    }

    @Schema(description = "Orden de compra")
    public record OrderResponse(
            @Schema(example = "1") Long id,
            @Schema(example = "Ash Ketchum") String userName,
            @Schema(example = "ash@pokemon.com") String userEmail,
            @Schema(description = "Total en CLP calculado por el servidor", example = "15980") Integer total,
            @Schema(example = "2026-11-12T04:20:00Z") Instant createdAt,
            List<OrderItemResponse> items) {
        public static OrderResponse from(PurchaseOrder o) {
            return new OrderResponse(o.getId(), o.getUser().getName(), o.getUser().getEmail(), o.getTotal(),
                    o.getCreatedAt(), o.getItems().stream().map(OrderItemResponse::from).toList());
        }
    }
}
