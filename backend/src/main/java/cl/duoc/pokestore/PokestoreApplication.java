package cl.duoc.pokestore;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.security.servlet.UserDetailsServiceAutoConfiguration;

// Se excluye el usuario "user" con contraseña autogenerada que Spring Security crea por defecto
@SpringBootApplication(exclude = UserDetailsServiceAutoConfiguration.class)
public class PokestoreApplication {

    public static void main(String[] args) {
        SpringApplication.run(PokestoreApplication.class, args);
    }
}
