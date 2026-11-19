package cl.duoc.pokestore.dto;

import cl.duoc.pokestore.entity.Category;
import cl.duoc.pokestore.entity.Product;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;

import java.util.Comparator;
import java.util.List;

public final class CatalogDtos {

    private CatalogDtos() {
    }

    @Schema(description = "Datos de un tipo de Pokémon")
    public record CategoryRequest(
            @Schema(example = "ice") @NotBlank @Size(max = 30) String name,
            @Schema(example = "#98d8d8", pattern = "^#[0-9a-fA-F]{6}$")
            @NotBlank @Pattern(regexp = "^#[0-9a-fA-F]{6}$", message = "debe tener formato #RRGGBB") String color) {
    }

    @Schema(description = "Tipo de Pokémon")
    public record CategoryResponse(
            @Schema(example = "3") Long id,
            @Schema(example = "fire") String name,
            @Schema(example = "#f08030") String color) {
        public static CategoryResponse from(Category c) {
            return new CategoryResponse(c.getId(), c.getName(), c.getColor());
        }
    }

    @Schema(description = "Datos de un Pokémon a la venta")
    public record ProductRequest(
            @Schema(description = "N° de la Pokédex (1-1025)", example = "6")
            @NotNull @Min(1) @Max(1025) Integer pokedexNumber,
            @Schema(example = "Charizard") @NotBlank @Size(max = 50) String name,
            @Schema(description = "Precio en CLP", example = "24990") @NotNull @Min(0) @Max(10_000_000) Integer price,
            @Schema(example = "3") @NotNull @Min(0) @Max(100_000) Integer stock,
            // Solo https: evita URLs "javascript:" o "data:" en el src de la imagen
            @Schema(description = "URL https de la imagen. Vacío = imagen oficial de PokeAPI según la Pokédex",
                    example = "", nullable = true)
            @Size(max = 500) @Pattern(regexp = "^$|^https://\\S+$", message = "debe ser una URL https") String imageUrl,
            @ArraySchema(arraySchema = @Schema(description = "IDs de los tipos (1 o 2)", example = "[3, 9]"),
                    minItems = 1, maxItems = 2)
            @NotEmpty(message = "debe tener al menos un tipo") @Size(max = 2, message = "máximo 2 tipos")
            List<@NotNull Long> categoryIds) {
    }

    @Schema(description = "Pokémon a la venta")
    public record ProductResponse(
            @Schema(example = "4") Long id,
            @Schema(example = "25") Integer pokedexNumber,
            @Schema(example = "Pikachu") String name,
            @Schema(example = "7990") Integer price,
            @Schema(example = "5") Integer stock,
            @Schema(example = "https://raw.githubusercontent.com/PokeAPI/sprites/master/sprites/pokemon/other/official-artwork/25.png")
            String imageUrl,
            List<CategoryResponse> categories) {
        public static ProductResponse from(Product p) {
            return new ProductResponse(
                    p.getId(), p.getPokedexNumber(), p.getName(), p.getPrice(), p.getStock(), p.getImageUrl(),
                    p.getCategories().stream()
                            .sorted(Comparator.comparing(Category::getName))
                            .map(CategoryResponse::from)
                            .toList());
        }
    }
}
