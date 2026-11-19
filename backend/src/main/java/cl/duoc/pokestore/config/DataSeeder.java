package cl.duoc.pokestore.config;

import cl.duoc.pokestore.entity.Category;
import cl.duoc.pokestore.entity.Product;
import cl.duoc.pokestore.entity.Role;
import cl.duoc.pokestore.entity.User;
import cl.duoc.pokestore.repository.CategoryRepository;
import cl.duoc.pokestore.repository.ProductRepository;
import cl.duoc.pokestore.repository.RoleRepository;
import cl.duoc.pokestore.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Map;
import java.util.stream.Collectors;

/** Carga datos iniciales SOLO si las tablas están vacías. */
@Slf4j
@Component
@RequiredArgsConstructor
public class DataSeeder implements CommandLineRunner {

    private final RoleRepository roleRepository;
    private final UserRepository userRepository;
    private final CategoryRepository categoryRepository;
    private final ProductRepository productRepository;
    private final PasswordEncoder passwordEncoder;

    @Value("${app.seed.admin-email}")
    private String adminEmail;
    @Value("${app.seed.admin-password}")
    private String adminPassword;
    @Value("${app.seed.user-email}")
    private String userEmail;
    @Value("${app.seed.user-password}")
    private String userPassword;

    @Override
    @Transactional
    public void run(String... args) {
        Role admin = roleRepository.findByName(Role.ADMIN).orElseGet(() -> roleRepository.save(new Role(Role.ADMIN)));
        Role user = roleRepository.findByName(Role.USER).orElseGet(() -> roleRepository.save(new Role(Role.USER)));

        if (userRepository.count() == 0) {
            createUser("Profe Oak", adminEmail, adminPassword, admin);
            createUser("Ash Ketchum", userEmail, userPassword, user);
            log.warn("Usuarios de prueba creados ({} / {}). Cambia sus contraseñas fuera de desarrollo.",
                    adminEmail, userEmail);
        }

        if (categoryRepository.count() == 0) {
            Map<String, String> types = Map.ofEntries(
                    Map.entry("grass", "#78c850"), Map.entry("poison", "#a040a0"), Map.entry("fire", "#f08030"),
                    Map.entry("water", "#6890f0"), Map.entry("electric", "#e0b800"), Map.entry("normal", "#a8a878"),
                    Map.entry("fairy", "#ee99ac"), Map.entry("ghost", "#705898"), Map.entry("flying", "#a890f0"),
                    Map.entry("dragon", "#7038f8"), Map.entry("psychic", "#f85888"), Map.entry("fighting", "#c03028"),
                    Map.entry("steel", "#8f8fa8"));
            types.forEach((name, color) -> categoryRepository.save(new Category(name, color)));
        }

        if (productRepository.count() == 0) {
            Map<String, Category> cats = categoryRepository.findAll().stream()
                    .collect(Collectors.toMap(Category::getName, c -> c));
            createProduct(cats, 1, "Bulbasaur", 4990, 10, "grass", "poison");
            createProduct(cats, 4, "Charmander", 5490, 8, "fire");
            createProduct(cats, 7, "Squirtle", 4990, 12, "water");
            createProduct(cats, 25, "Pikachu", 7990, 5, "electric");
            createProduct(cats, 39, "Jigglypuff", 3990, 15, "normal", "fairy");
            createProduct(cats, 94, "Gengar", 12990, 4, "ghost", "poison");
            createProduct(cats, 130, "Gyarados", 14990, 3, "water", "flying");
            createProduct(cats, 133, "Eevee", 6490, 9, "normal");
            createProduct(cats, 143, "Snorlax", 10990, 2, "normal");
            createProduct(cats, 149, "Dragonite", 19990, 2, "dragon", "flying");
            createProduct(cats, 150, "Mewtwo", 29990, 1, "psychic");
            createProduct(cats, 448, "Lucario", 15990, 6, "fighting", "steel");
        }
    }

    private void createUser(String name, String email, String password, Role role) {
        User u = new User();
        u.setName(name);
        u.setEmail(User.normalizeEmail(email));
        u.setPassword(passwordEncoder.encode(password));
        u.setRole(role);
        userRepository.save(u);
    }

    private void createProduct(Map<String, Category> cats, int dex, String name, int price, int stock, String... types) {
        Product p = new Product();
        p.setPokedexNumber(dex);
        p.setName(name);
        p.setPrice(price);
        p.setStock(stock);
        p.setImageUrl(Product.defaultImage(dex));
        p.setCategories(Arrays.stream(types).map(cats::get).collect(Collectors.toCollection(HashSet::new)));
        productRepository.save(p);
    }
}
