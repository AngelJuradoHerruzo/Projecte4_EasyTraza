package cat.copernic.easytraza.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import cat.copernic.easytraza.entities.Usuari;
import cat.copernic.easytraza.service.UsuariService;

@Controller
@RequestMapping("/usuaris")
public class UsuariWebController {

    private final UsuariService usuariService;

    public UsuariWebController(UsuariService usuariService) {
        this.usuariService = usuariService;
    }

    @GetMapping("/list")
    public String llistarUsuaris(Model model) {
        model.addAttribute("usuaris", usuariService.getAllUsuaris());
        return "usuaris/llistarUsuaris";
    }

    @GetMapping("/new")
    public String formCrearUsuari(Model model) {
        model.addAttribute("usuari", new Usuari());
        return "usuaris/formUsuaris";
    }

    @PostMapping
    public String guardarUsuari(@ModelAttribute Usuari usuari) {
        usuariService.createUsuari(usuari);
        return "redirect:/usuaris/list";
    }

    @GetMapping("/edit/{id}")
    public String formEditarUsuari(@PathVariable Long id, Model model) {
        model.addAttribute("usuari", usuariService.getUsuariById(id));
        return "usuaris/formUsuaris";
    }

    @PostMapping("/update/{id}")
    public String updateUsuari(@PathVariable Long id, @ModelAttribute Usuari usuari) {
        usuariService.updateUsuari(id, usuari);
        return "redirect:/usuaris/list";
    }

    @GetMapping("/delete/{id}")
    public String deleteUsuari(@PathVariable Long id) {
        usuariService.deleteUsuari(id);
        return "redirect:/usuaris/list";
    }
}