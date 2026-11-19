package cl.duoc.pokestore.security;

/** Usuario autenticado de la petición actual (se obtiene con @AuthenticationPrincipal). */
public record AuthUser(Long id, String email, String role) {
}
