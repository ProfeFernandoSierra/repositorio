package cl.duoc.pokestore.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "order_items")
@Getter
@Setter
@NoArgsConstructor
public class OrderItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "order_id")
    private PurchaseOrder order;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "product_id")
    private Product product;

    /** Copia del nombre y precio al momento de comprar (si después cambia el precio, la orden no cambia). */
    @Column(nullable = false, length = 50)
    private String productName;

    @Column(nullable = false)
    private Integer unitPrice;

    @Column(nullable = false)
    private Integer quantity;
}
