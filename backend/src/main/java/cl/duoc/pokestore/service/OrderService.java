package cl.duoc.pokestore.service;

import cl.duoc.pokestore.dto.OrderDtos.OrderItemRequest;
import cl.duoc.pokestore.dto.OrderDtos.OrderRequest;
import cl.duoc.pokestore.dto.OrderDtos.OrderResponse;
import cl.duoc.pokestore.entity.OrderItem;
import cl.duoc.pokestore.entity.Product;
import cl.duoc.pokestore.entity.PurchaseOrder;
import cl.duoc.pokestore.entity.User;
import cl.duoc.pokestore.exception.ApiException;
import cl.duoc.pokestore.repository.OrderRepository;
import cl.duoc.pokestore.repository.ProductRepository;
import cl.duoc.pokestore.repository.UserRepository;
import cl.duoc.pokestore.security.AuthUser;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.TreeMap;

@Service
@RequiredArgsConstructor
@Transactional
public class OrderService {

    private static final int MAX_PER_PRODUCT = 99;

    private final OrderRepository orderRepository;
    private final ProductRepository productRepository;
    private final UserRepository userRepository;

    /**
     * Crea la orden en UNA transacción: valida stock, lo descuenta y calcula el total
     * con los precios de la BD. Si algo falla, no se guarda nada (rollback).
     */
    public OrderResponse create(AuthUser me, OrderRequest req) {
        User user = userRepository.findById(me.id())
                .orElseThrow(() -> ApiException.unauthorized("Usuario no existe"));

        // Agrupar productos repetidos. TreeMap = orden por id, así los locks se toman siempre
        // en el mismo orden y se evitan deadlocks entre compras simultáneas.
        Map<Long, Integer> quantities = new TreeMap<>();
        for (OrderItemRequest item : req.items()) {
            quantities.merge(item.productId(), item.quantity(), Integer::sum);
        }

        PurchaseOrder order = new PurchaseOrder();
        order.setUser(user);
        int total = 0;

        for (Map.Entry<Long, Integer> entry : quantities.entrySet()) {
            int quantity = entry.getValue();
            if (quantity > MAX_PER_PRODUCT) {
                throw ApiException.badRequest("Máximo " + MAX_PER_PRODUCT + " unidades por Pokémon");
            }
            Product product = productRepository.findByIdForUpdate(entry.getKey())
                    .orElseThrow(() -> ApiException.notFound("El Pokémon " + entry.getKey() + " no existe"));
            if (product.getStock() < quantity) {
                throw ApiException.conflict("Stock insuficiente para " + product.getName()
                        + " (quedan " + product.getStock() + ")");
            }
            product.setStock(product.getStock() - quantity);

            OrderItem item = new OrderItem();
            item.setProduct(product);
            item.setProductName(product.getName());
            item.setUnitPrice(product.getPrice());
            item.setQuantity(quantity);
            order.addItem(item);

            total = Math.addExact(total, Math.multiplyExact(product.getPrice(), quantity));
        }

        order.setTotal(total);
        return OrderResponse.from(orderRepository.save(order));
    }

    @Transactional(readOnly = true)
    public List<OrderResponse> mine(AuthUser me) {
        return orderRepository.findByUser_IdOrderByCreatedAtDesc(me.id()).stream().map(OrderResponse::from).toList();
    }

    @Transactional(readOnly = true)
    public List<OrderResponse> findAll() {
        return orderRepository.findAllByOrderByCreatedAtDesc().stream().map(OrderResponse::from).toList();
    }
}
