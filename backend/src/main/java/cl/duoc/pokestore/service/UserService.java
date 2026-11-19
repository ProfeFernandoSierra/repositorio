package cl.duoc.pokestore.service;

import cl.duoc.pokestore.dto.UserDtos.RoleResponse;
import cl.duoc.pokestore.dto.UserDtos.UserRequest;
import cl.duoc.pokestore.dto.UserDtos.UserResponse;
import cl.duoc.pokestore.entity.Role;
import cl.duoc.pokestore.entity.User;
import cl.duoc.pokestore.exception.ApiException;
import cl.duoc.pokestore.repository.OrderRepository;
import cl.duoc.pokestore.repository.RoleRepository;
import cl.duoc.pokestore.repository.UserRepository;
import cl.duoc.pokestore.security.AuthUser;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class UserService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final OrderRepository orderRepository;
    private final PasswordEncoder passwordEncoder;

    @Transactional(readOnly = true)
    public List<UserResponse> findAll() {
        return userRepository.findAll(Sort.by("name")).stream().map(UserResponse::from).toList();
    }

    @Transactional(readOnly = true)
    public UserResponse findById(Long id) {
        return UserResponse.from(get(id));
    }

    @Transactional(readOnly = true)
    public List<RoleResponse> roles() {
        return roleRepository.findAll(Sort.by("name")).stream().map(RoleResponse::from).toList();
    }

    public UserResponse create(UserRequest req) {
        if (req.password() == null || req.password().isBlank()) {
            throw ApiException.badRequest("La contraseña es obligatoria");
        }
        String email = User.normalizeEmail(req.email());
        if (userRepository.existsByEmail(email)) {
            throw ApiException.conflict("El email ya está registrado");
        }
        User user = new User();
        user.setName(req.name().trim());
        user.setEmail(email);
        user.setRole(findRole(req.role()));
        user.setPassword(passwordEncoder.encode(req.password()));
        return UserResponse.from(userRepository.save(user));
    }

    public UserResponse update(Long id, UserRequest req, AuthUser me) {
        User user = get(id);
        String email = User.normalizeEmail(req.email());
        if (userRepository.existsByEmailAndIdNot(email, id)) {
            throw ApiException.conflict("El email ya está registrado");
        }
        Role newRole = findRole(req.role());
        if (user.isAdmin() && !Role.ADMIN.equals(newRole.getName())) {
            if (user.getId().equals(me.id())) {
                throw ApiException.badRequest("No puedes quitarte el rol de administrador a ti mismo");
            }
            ensureNotLastAdmin();
        }
        user.setName(req.name().trim());
        user.setEmail(email);
        user.setRole(newRole);
        if (req.password() != null && !req.password().isBlank()) {
            user.setPassword(passwordEncoder.encode(req.password()));
        }
        return UserResponse.from(user);
    }

    public void delete(Long id, AuthUser me) {
        if (id.equals(me.id())) {
            throw ApiException.badRequest("No puedes eliminar tu propia cuenta");
        }
        User user = get(id);
        if (user.isAdmin()) {
            ensureNotLastAdmin();
        }
        if (orderRepository.existsByUser_Id(id)) {
            throw ApiException.conflict("El usuario tiene compras registradas; no se puede eliminar");
        }
        userRepository.delete(user);
    }

    private void ensureNotLastAdmin() {
        if (userRepository.countByRole_Name(Role.ADMIN) <= 1) {
            throw ApiException.badRequest("Debe existir al menos un administrador");
        }
    }

    private Role findRole(String name) {
        return roleRepository.findByName(name.trim().toUpperCase())
                .orElseThrow(() -> ApiException.badRequest("Rol inválido: " + name));
    }

    private User get(Long id) {
        return userRepository.findById(id).orElseThrow(() -> ApiException.notFound("Usuario no encontrado"));
    }
}
