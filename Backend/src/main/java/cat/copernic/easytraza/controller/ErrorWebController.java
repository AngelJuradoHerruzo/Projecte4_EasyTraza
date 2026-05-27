package cat.copernic.easytraza.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * CONTROLADOR WEB D'ERRORS.
 *
 * Gestionades les pantalles mostrades quan es produeixen errors d'accés,
 * de recurs no trobat o errors interns del servidor.
 *
 * @author Ángel Jurado Herruz
 */
@Controller
public class ErrorWebController {


    /**
     * ERROR D'ACCÉS DENEGAT.
     *
     * Mostrada la vista informativa quan l'usuari no disposa
     * d'autorització per accedir al recurs sol·licitat.
     *
     * @return vista corresponent a l'error d'accés denegat
     */
    @GetMapping("/error/403")
    public String error403() {
        return "error/403";
    }


    /**
     * ERROR DE PÀGINA NO TROBADA.
     *
     * Mostrada la vista informativa quan no existeix el recurs
     * sol·licitat per l'usuari.
     *
     * @return vista corresponent a l'error de pàgina no trobada
     */
    @GetMapping("/error/404")
    public String error404() {
        return "error/404";
    }


    /**
     * ERROR INTERN DEL SERVIDOR.
     *
     * Mostrada la vista informativa quan es produeix un error intern
     * durant el processament de la petició.
     *
     * @return vista corresponent a l'error intern del servidor
     */
    @GetMapping("/error/500")
    public String error500() {
        return "error/500";
    }
}