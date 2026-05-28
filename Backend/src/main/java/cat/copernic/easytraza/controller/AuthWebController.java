package cat.copernic.easytraza.controller;

import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * CONTROLADOR D'AUTENTICACIÓ WEB.
 *
 * Gestionades les pantalles bàsiques d'entrada al sistema i la redirecció
 * inicial segons el rol de l'usuari autenticat.
 *
 * @author Ángel Jurado Herruzo
 */
@Controller
public class AuthWebController {


    /**
     * REDIRECCIÓ INICIAL.
     *
     * Redirigida la navegació inicial a la pantalla corresponent segons
     * el rol de l'usuari autenticat.
     *
     * @param authentication autenticació activa de l'usuari
     * @return redirecció a la pantalla inicial corresponent
     */
    @GetMapping("/")
    public String index(Authentication authentication) {

        if (esOperari(authentication)) {
            return "redirect:/albarans-proveidor/list";
        }

        return "redirect:/productes/list";
    }


    /**
     * ACCÉS AL FORMULARI DE LOGIN.
     *
     * Mostrada la pantalla de login quan no hi ha una sessió autenticada
     * o redirigit l'usuari que ja ha iniciat sessió.
     *
     * @param authentication autenticació activa de l'usuari
     * @return vista del login o redirecció inicial
     */
    @GetMapping("/login")
    public String login(Authentication authentication) {

        if (authentication != null && authentication.isAuthenticated()) {
            return index(authentication);
        }

        return "auth/login";
    }


    /**
     * COMPROVACIÓ DEL ROL OPERARI.
     *
     * Comprovat si l'usuari autenticat disposa del rol d'operari.
     *
     * @param authentication autenticació activa de l'usuari
     * @return cert si l'usuari autenticat és operari
     */
    private boolean esOperari(Authentication authentication) {

        if (authentication == null || !authentication.isAuthenticated()) {
            return false;
        }

        return authentication.getAuthorities().stream()
                .anyMatch(authority -> "ROLE_OPERARI".equals(authority.getAuthority()));
    }
}
