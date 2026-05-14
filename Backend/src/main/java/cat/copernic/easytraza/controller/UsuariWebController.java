package cat.copernic.easytraza.controller;

import java.util.List;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import cat.copernic.easytraza.entities.Usuari;
import cat.copernic.easytraza.service.UsuariService;
import jakarta.servlet.http.HttpSession;

@Controller
@RequestMapping("/usuaris")
public class UsuariWebController {

    // ---------------------------- SERVICE I CONSTRUCTOR ----------------------------
    private final UsuariService usuariService;

    public UsuariWebController(UsuariService usuariService) {
        this.usuariService = usuariService;
    }


    // LLISTAR USUARIS
    @GetMapping("/list")
    public String llistarUsuaris(Model model, HttpSession session) {

        Long usuariId = (Long) session.getAttribute("usuariId");

        List<Usuari> usuaris = usuariService.getAllUsuaris()
                .stream()
                .filter(usuari -> usuariId == null || !usuari.getId().equals(usuariId))
                .toList();

        model.addAttribute("usuaris", usuaris);

        return "usuaris/llistarUsuaris";
    }


    // FORMULARI CREAR USUARI
    @GetMapping("/new")
    public String formCrearUsuari(Model model) {
        model.addAttribute("usuari", new Usuari());
        return "usuaris/formUsuaris";
    }


    // GUARDAR USUARI
    @PostMapping("/save")
    public String guardarUsuari(@ModelAttribute("usuari") Usuari usuari, Model model) {
        try {
            usuariService.createUsuari(usuari);
            return "redirect:/usuaris/list";
        }
        catch (RuntimeException e) {
            model.addAttribute("error", e.getMessage());
            model.addAttribute("usuari", usuari);
            return "usuaris/formUsuaris";
        }
    }


    // FORMULARI EDITAR USUARI
    @GetMapping("/edit/{id}")
    public String formEditarUsuari(@PathVariable Long id, Model model) {
        Usuari usuari = usuariService.getUsuariById(id);

        if (usuari == null) {
            return "redirect:/usuaris/list";
        }

        model.addAttribute("usuari", usuari);
        return "usuaris/formUsuaris";
    }


    // ACTUALITZAR USUARI
    @PostMapping("/update/{id}")
    public String updateUsuari(@PathVariable Long id,
                               @ModelAttribute("usuari") Usuari usuari,
                               Model model) {
        try {
            usuariService.updateUsuari(id, usuari);
            return "redirect:/usuaris/list";
        }
        catch (RuntimeException e) {
            usuari.setId(id);
            model.addAttribute("error", e.getMessage());
            model.addAttribute("usuari", usuari);
            return "usuaris/formUsuaris";
        }
    }


    // ELIMINAR USUARI
    @GetMapping("/delete/{id}")
    public String deleteUsuari(@PathVariable Long id) {
        usuariService.deleteUsuari(id);
        return "redirect:/usuaris/list";
    }
}