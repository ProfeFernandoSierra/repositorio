package cl.duoc.pokestore.service;

import cl.duoc.pokestore.dto.AuthDtos.AuthResponse;
import cl.duoc.pokestore.dto.AuthDtos.LoginRequest;
import cl.duoc.pokestore.dto.AuthDtos.RegisterRequest;
import cl.duoc.pokestore.dto.UserDtos.UserResponse;
import cl.duoc.pokestore.entity.Role;
import cl.duoc.pokestore.entity.User;
import cl.duoc.pokestore.exception.ApiException;
import cl.duoc.pokestore.repository.RoleRepository;
import cl.duoc.pokestore.repository.UserRepository;
import cl.duoc.pokestore.security.AuthUser;
import cl.duoc.pokestore.security.JwtService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    /** Hash de relleno: si el email no existe igual se compara un hash, así la respuesta tarda lo mismo. */
    private final String dummyHash;

    public AuthService(UserRepository userRepository, RoleRepository roleRepository,
                       PasswordEncoder passwordEncoder, JwtService jwtService) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
        this.dummyHash = passwordEncoder.encode("dummy-password-no-usar");
    }

    @Transactional(readOnly = true)
    public AuthResponse login(LoginRequest req) {
        Optional<User> user = userRepository.findByEmail(User.normalizeEmail(req.email()));
        boolean matches = passwordEncoder.matches(req.password(), user.map(User::getPassword).orElse(dummyHash));
        if (user.isEmpty() || !matches) {
            // Mensaje genérico: no revelar si el email existe
            throw ApiException.unauthorized("Credenciales inválidas");
        }
        return new AuthResponse(jwtService.generate(user.get()), UserResponse.from(user.get()));
    }

    @Transactional
    public AuthResponse register(RegisterRequest req) {
        String email = User.normalizeEmail(req.email());
        if (userRepository.existsByEmail(email)) {
            throw ApiException.conflict("El email ya está registrado");
        }
        Role role = roleRepository.findByName(Role.USER)
                .orElseThrow(() -> new IllegalStateException("Falta el rol USER"));
        User user = new User();
        user.setName(req.name().trim());
        user.setEmail(email);
        user.setPassword(passwordEncoder.encode(req.password()));
        user.setRole(role);
        userRepository.save(user);
        return new AuthResponse(jwtService.generate(user), UserResponse.from(user));
    }

    @Transactional(readOnly = true)
    public UserResponse me(AuthUser me) {
        return userRepository.findById(me.id())
                .map(UserResponse::from)
                .orElseThrow(() -> ApiException.unauthorized("Usuario no existe"));
    }
}
