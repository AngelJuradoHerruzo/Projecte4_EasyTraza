package cat.copernic.easytraza.controller;

import cat.copernic.easytraza.entities.AlbaraProveidor;
import cat.copernic.easytraza.service.AlbaraProveidorService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/albarans-proveidor")
public class AlbaraProveidorWebController {

    // ---------------------------- SERVICE ----------------------------
    private final AlbaraProveidorService albaraProveidorService;

    public AlbaraProveidorWebController(AlbaraProveidorService albaraProveidorService) {
        this.albaraProveidorService = albaraProveidorService;
    }


    // ---------------------------- LLISTAR ----------------------------
    @GetMapping
    public String list(Model model) {
        model.addAttribute("albarans", albaraProveidorService.getAllAlbaransProveidor());
        return "albaransProveidor/llistarAlbaransProveidor";
    }


    // ---------------------------- FORM NOU ----------------------------
    @GetMapping("/new")
    public String newAlbara(Model model) {
        model.addAttribute("albara", new AlbaraProveidor());
        return "albaransProveidor/formAlbaraProveidor";
    }


    // ---------------------------- GUARDAR ----------------------------
    @PostMapping("/save")
    public String save(@ModelAttribute AlbaraProveidor albara, Model model) {

        try {
            albaraProveidorService.createAlbaraProveidor(albara);
            return "redirect:/albarans-proveidor";
        } 
        catch (RuntimeException e) {
            model.addAttribute("error", e.getMessage());
            model.addAttribute("albara", albara);
            return "albaransProveidor/formAlbaraProveidor";
        }
    }


    // ---------------------------- EDITAR ----------------------------
    @GetMapping("/edit/{id}")
    public String edit(@PathVariable Long id, Model model) {

        AlbaraProveidor albara = albaraProveidorService.getAlbaraProveidorById(id);

        if (albara == null) {
            return "redirect:/albarans-proveidor";
        }

        model.addAttribute("albara", albara);
        return "albaransProveidor/formAlbaraProveidor";
    }


    // ---------------------------- ACTUALITZAR ----------------------------
    @PostMapping("/update/{id}")
    public String update(@PathVariable Long id,
                         @ModelAttribute AlbaraProveidor albara,
                         Model model) {

        try {
            albaraProveidorService.updateAlbaraProveidor(id, albara);
            return "redirect:/albarans-proveidor";
        } 
        catch (RuntimeException e) {
            model.addAttribute("error", e.getMessage());
            model.addAttribute("albara", albara);
            return "albaransProveidor/formAlbaraProveidor";
        }
    }


    // ---------------------------- ELIMINAR ----------------------------
    @GetMapping("/delete/{id}")
    public String delete(@PathVariable Long id) {
        albaraProveidorService.deleteAlbaraProveidor(id);
        return "redirect:/albarans-proveidor";
    }
}