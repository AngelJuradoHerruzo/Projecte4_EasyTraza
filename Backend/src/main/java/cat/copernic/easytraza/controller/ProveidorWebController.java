package cat.copernic.easytraza.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import cat.copernic.easytraza.entities.Proveidor;
import cat.copernic.easytraza.service.ProveidorService;

@Controller
@RequestMapping("/proveidors")
public class ProveidorWebController {

    // ---------------------------- SERVICE I CONSTRUCTOR ----------------------------
    private final ProveidorService proveidorService;

    public ProveidorWebController(ProveidorService proveidorService) {
        this.proveidorService = proveidorService;
    }


    // LLISTAR PROVEÏDORS
    @GetMapping("/list")
    public String llistarProveidors(Model model) {
        model.addAttribute("proveidors", proveidorService.getAllProveidors());
        return "proveidors/llistarProveidors";
    }


    // FORMULARI CREAR PROVEÏDOR
    @GetMapping("/new")
    public String formCrearProveidor(Model model) {
        model.addAttribute("proveidor", new Proveidor());
        return "proveidors/formProveidors";
    }


    // GUARDAR PROVEÏDOR
    @PostMapping("/save")
    public String guardarProveidor(@ModelAttribute("proveidor") Proveidor proveidor, Model model) {
        try {
            proveidorService.createProveidor(proveidor);
            return "redirect:/proveidors/list";
        }
        catch (RuntimeException e) {
            model.addAttribute("error", e.getMessage());
            model.addAttribute("proveidor", proveidor);
            return "proveidors/formProveidors";
        }
    }


    // FORMULARI EDITAR PROVEÏDOR
    @GetMapping("/edit/{id}")
    public String formEditarProveidor(@PathVariable Long id, Model model) {
        Proveidor proveidor = proveidorService.getProveidorById(id);

        if (proveidor == null) {
            return "redirect:/proveidors/list";
        }

        model.addAttribute("proveidor", proveidor);
        return "proveidors/formProveidors";
    }


    // ACTUALITZAR PROVEÏDOR
    @PostMapping("/update/{id}")
    public String updateProveidor(@PathVariable Long id,
                                  @ModelAttribute("proveidor") Proveidor proveidor,
                                  Model model) {
        try {
            proveidorService.updateProveidor(id, proveidor);
            return "redirect:/proveidors/list";
        }
        catch (RuntimeException e) {
            model.addAttribute("error", e.getMessage());
            model.addAttribute("proveidor", proveidor);
            return "proveidors/formProveidors";
        }
    }


    // ELIMINAR PROVEÏDOR
    @GetMapping("/delete/{id}")
    public String deleteProveidor(@PathVariable Long id) {
        proveidorService.deleteProveidor(id);
        return "redirect:/proveidors/list";
    }
}