package cl.duoc.pokestore.repository;

import cl.duoc.pokestore.entity.PurchaseOrder;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface OrderRepository extends JpaRepository<PurchaseOrder, Long> {
    List<PurchaseOrder> findByUser_IdOrderByCreatedAtDesc(Long userId);

    List<PurchaseOrder> findAllByOrderByCreatedAtDesc();

    boolean existsByUser_Id(Long userId);
}
