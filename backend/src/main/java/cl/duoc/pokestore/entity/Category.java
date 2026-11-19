package cl.duoc.pokestore.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/** Categoría de producto = tipo de Pokémon (fuego, agua, planta...). */
@Entity
@Table(name = "categories")
@Getter
@Setter
@NoArgsConstructor
public class Category {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 30)
    private String name;

    /** Color en formato #RRGGBB para mostrar la etiqueta en el front. */
    @Column(nullable = false, length = 7)
    private String color;

    public Category(String name, String color) {
        this.name = name;
        this.color = color;
    }
}
