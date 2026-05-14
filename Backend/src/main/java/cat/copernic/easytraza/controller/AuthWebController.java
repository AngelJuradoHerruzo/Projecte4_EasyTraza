package cat.copernic.easytraza.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

import java.security.Principal;

/**
 * CONTROLADOR D'AUTENTICACIÓ WEB
 *
 * Gestiona les pantalles bàsiques d'entrada al sistema.
 *
 * @author Ángel Jurado
 */
@Controller
public class AuthWebController {

    // REDIRECCIÓ INICIAL
    @GetMapping("/")
    public String index() {
        return "redirect:/productes/list";
    }


    // FORMULARI DE LOGIN
    @GetMapping("/login")
    public String login(Principal principal) {

        if (principal != null) {
            return "redirect:/productes/list";
        }

        return "auth/login";
    }
}