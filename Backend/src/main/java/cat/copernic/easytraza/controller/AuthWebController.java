package cat.copernic.easytraza.controller;

import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * CONTROLADOR D'AUTENTICACIÓ WEB
 *
 * Gestiona les pantalles bàsiques d'entrada al sistema.
 *
 * @author Ángel Jurado
 */
@Controller
public class AuthWebController {

    // REDIRECCIÓ INICIAL SEGONS EL ROL DE L'USUARI
    @GetMapping("/")
    public String index(Authentication authentication) {

        if (esOperari(authentication)) {
            return "redirect:/albarans-proveidor/list";
        }

        return "redirect:/productes/list";
    }


    // FORMULARI DE LOGIN
    @GetMapping("/login")
    public String login(Authentication authentication) {

        if (authentication != null && authentication.isAuthenticated()) {
            return index(authentication);
        }

        return "auth/login";
    }


    // COMPROVAR SI L'USUARI AUTENTICAT ÉS OPERARI
    private boolean esOperari(Authentication authentication) {

        if (authentication == null || !authentication.isAuthenticated()) {
            return false;
        }

        return authentication.getAuthorities().stream()
                .anyMatch(authority -> "ROLE_OPERARI".equals(authority.getAuthority()));
    }
}
