package cat.copernic.easytraza.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class ErrorWebController {

    // ERROR 403 - ACCÉS DENEGAT
    @GetMapping("/error/403")
    public String error403() {
        return "error/403";
    }

    // ERROR 404 - PÀGINA NO TROBADA
    @GetMapping("/error/404")
    public String error404() {
        return "error/404";
    }

    // ERROR 500 - ERROR INTERN DEL SERVIDOR
    @GetMapping("/error/500")
    public String error500() {
        return "error/500";
    }
}