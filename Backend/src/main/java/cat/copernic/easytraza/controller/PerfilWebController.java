package cat.copernic.easytraza.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import cat.copernic.easytraza.entities.Usuari;
import cat.copernic.easytraza.service.UsuariService;
import jakarta.servlet.http.HttpSession;

/**
 * CONTROLADOR WEB DEL PERFIL D'USUARI
 *
 * Gestiona la visualització i modificació del perfil de l'usuari autenticat.
 *
 * @author Ángel Jurado
 */
@Controller
@RequestMapping("/perfil")
public class PerfilWebController {

    // ---------------------------- SERVICE I CONSTRUCTOR ----------------------------
    private final UsuariService usuariService;

    public PerfilWebController(UsuariService usuariService) {
        this.usuariService = usuariService;
    }


    // VISUALITZAR PERFIL
    @GetMapping
    public String veurePerfil(Model model, HttpSession session) {

        Long usuariId = (Long) session.getAttribute("usuariId");

        if (usuariId == null) {
            return "redirect:/login";
        }

        Usuari usuari = usuariService.getUsuariById(usuariId);

        if (usuari == null) {
            session.invalidate();
            return "redirect:/login";
        }

        model.addAttribute("usuari", usuari);

        return "perfil/veurePerfil";
    }


    // FORMULARI D'EDICIÓ DEL PERFIL
    @GetMapping("/edit")
    public String editarPerfil(Model model, HttpSession session) {

        Long usuariId = (Long) session.getAttribute("usuariId");

        if (usuariId == null) {
            return "redirect:/login";
        }

        Usuari usuari = usuariService.getUsuariById(usuariId);

        if (usuari == null) {
            session.invalidate();
            return "redirect:/login";
        }

        usuari.setPassword(null);

        model.addAttribute("usuari", usuari);

        return "perfil/formPerfil";
    }


    // ACTUALITZAR PERFIL
    @PostMapping("/update")
    public String updatePerfil(@ModelAttribute("usuari") Usuari usuari,
                               Model model,
                               HttpSession session) {

        try {
            Long usuariId = (Long) session.getAttribute("usuariId");

            if (usuariId == null) {
                return "redirect:/login";
            }

            Usuari usuariActualitzat = usuariService.updatePerfilUsuari(usuariId, usuari);

            session.setAttribute("usuariNom", usuariActualitzat.getNomComplet());
            session.setAttribute("usuariEmail", usuariActualitzat.getEmail());
            session.setAttribute("usuariRol", usuariActualitzat.getRolUsuari());

            return "redirect:/perfil?success=true";
        }
        catch (RuntimeException e) {
            model.addAttribute("error", e.getMessage());
            model.addAttribute("usuari", usuari);

            return "perfil/formPerfil";
        }
    }
}