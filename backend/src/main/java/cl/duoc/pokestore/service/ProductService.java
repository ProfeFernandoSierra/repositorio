package cl.duoc.pokestore.service;

import cl.duoc.pokestore.dto.CatalogDtos.ProductRequest;
import cl.duoc.pokestore.dto.CatalogDtos.ProductResponse;
import cl.duoc.pokestore.entity.Category;
import cl.duoc.pokestore.entity.Product;
import cl.duoc.pokestore.exception.ApiException;
import cl.duoc.pokestore.repository.CategoryRepository;
import cl.duoc.pokestore.repository.OrderItemRepository;
import cl.duoc.pokestore.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
@Transactional
public class ProductService {

    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;
    private final OrderItemRepository orderItemRepository;

    @Transactional(readOnly = true)
    public List<ProductResponse> findAll() {
        return productRepository.findAllByOrderByPokedexNumberAsc().stream().map(ProductResponse::from).toList();
    }

    @Transactional(readOnly = true)
    public ProductResponse findById(Long id) {
        return ProductResponse.from(get(id));
    }

    public ProductResponse create(ProductRequest req) {
        if (productRepository.existsByNameIgnoreCase(req.name().trim())) {
            throw ApiException.conflict("Ya existe un Pokémon llamado " + req.name().trim());
        }
        Product product = new Product();
        apply(product, req);
        return ProductResponse.from(productRepository.save(product));
    }

    public ProductResponse update(Long id, ProductRequest req) {
        Product product = get(id);
        if (productRepository.existsByNameIgnoreCaseAndIdNot(req.name().trim(), id)) {
            throw ApiException.conflict("Ya existe un Pokémon llamado " + req.name().trim());
        }
        apply(product, req);
        return ProductResponse.from(product);
    }

    public void delete(Long id) {
        Product product = get(id);
        if (orderItemRepository.existsByProduct_Id(id)) {
            throw ApiException.conflict("Este Pokémon tiene ventas registradas; deja su stock en 0 en vez de eliminarlo");
        }
        productRepository.delete(product);
    }

    private void apply(Product p, ProductRequest req) {
        Set<Long> ids = new HashSet<>(req.categoryIds());
        List<Category> categories = categoryRepository.findAllById(ids);
        if (categories.size() != ids.size()) {
            throw ApiException.badRequest("Algún tipo seleccionado no existe");
        }
        p.setPokedexNumber(req.pokedexNumber());
        p.setName(req.name().trim());
        p.setPrice(req.price());
        p.setStock(req.stock());
        p.setImageUrl(req.imageUrl() == null || req.imageUrl().isBlank()
                ? Product.defaultImage(req.pokedexNumber())
                : req.imageUrl().trim());
        p.setCategories(new HashSet<>(categories));
    }

    private Product get(Long id) {
        return productRepository.findById(id).orElseThrow(() -> ApiException.notFound("Pokémon no encontrado"));
    }
}
