package cl.duoc.pokestore.service;

import cl.duoc.pokestore.dto.CatalogDtos.CategoryRequest;
import cl.duoc.pokestore.dto.CatalogDtos.CategoryResponse;
import cl.duoc.pokestore.entity.Category;
import cl.duoc.pokestore.exception.ApiException;
import cl.duoc.pokestore.repository.CategoryRepository;
import cl.duoc.pokestore.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class CategoryService {

    private final CategoryRepository categoryRepository;
    private final ProductRepository productRepository;

    @Transactional(readOnly = true)
    public List<CategoryResponse> findAll() {
        return categoryRepository.findAllByOrderByNameAsc().stream().map(CategoryResponse::from).toList();
    }

    @Transactional(readOnly = true)
    public CategoryResponse findById(Long id) {
        return CategoryResponse.from(get(id));
    }

    public CategoryResponse create(CategoryRequest req) {
        String name = req.name().trim().toLowerCase();
        if (categoryRepository.existsByNameIgnoreCase(name)) {
            throw ApiException.conflict("Ya existe el tipo " + name);
        }
        return CategoryResponse.from(categoryRepository.save(new Category(name, req.color().toLowerCase())));
    }

    public CategoryResponse update(Long id, CategoryRequest req) {
        Category category = get(id);
        String name = req.name().trim().toLowerCase();
        if (categoryRepository.existsByNameIgnoreCaseAndIdNot(name, id)) {
            throw ApiException.conflict("Ya existe el tipo " + name);
        }
        category.setName(name);
        category.setColor(req.color().toLowerCase());
        return CategoryResponse.from(category);
    }

    public void delete(Long id) {
        Category category = get(id);
        if (productRepository.existsByCategories_Id(id)) {
            throw ApiException.conflict("Hay Pokémon con este tipo; quítaselo antes de eliminarlo");
        }
        categoryRepository.delete(category);
    }

    private Category get(Long id) {
        return categoryRepository.findById(id).orElseThrow(() -> ApiException.notFound("Tipo no encontrado"));
    }
}
