package cat.copernic.easytraza.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import cat.copernic.easytraza.entities.MateriaPrima;
import cat.copernic.easytraza.service.MateriaPrimaService;

@Controller
@RequestMapping("/materies-primeres")
public class MateriaPrimaWebController {

    // ---------------------------- SERVICE I CONSTRUCTOR ----------------------------
    private final MateriaPrimaService materiaPrimaService;

    public MateriaPrimaWebController(MateriaPrimaService materiaPrimaService) {
        this.materiaPrimaService = materiaPrimaService;
    }


    // LLISTAR MATÈRIES PRIMERES
    @GetMapping("/list")
    public String llistarMateriesPrimeres(Model model) {
        model.addAttribute("materiesPrimeres", materiaPrimaService.getAllMateriesPrimeres());
        return "materiesPrimeres/llistarMateriesPrimeres";
    }


    // FORMULARI CREAR MATÈRIA PRIMERA
    @GetMapping("/new")
    public String formCrearMateriaPrima(Model model) {
        model.addAttribute("materiaPrima", new MateriaPrima());
        return "materiesPrimeres/formMateriesPrimeres";
    }


    // GUARDAR MATÈRIA PRIMERA
    @PostMapping("/save")
    public String guardarMateriaPrima(@ModelAttribute("materiaPrima") MateriaPrima materiaPrima, Model model) {
        try {
            materiaPrimaService.createMateriaPrima(materiaPrima);
            return "redirect:/materies-primeres/list";
        }
        catch (RuntimeException e) {
            model.addAttribute("error", e.getMessage());
            model.addAttribute("materiaPrima", materiaPrima);
            return "materiesPrimeres/formMateriesPrimeres";
        }
    }


    // FORMULARI EDITAR MATÈRIA PRIMERA
    @GetMapping("/edit/{id}")
    public String formEditarMateriaPrima(@PathVariable Long id, Model model) {
        MateriaPrima materiaPrima = materiaPrimaService.getMateriaPrimaById(id);

        if (materiaPrima == null) {
            return "redirect:/materies-primeres/list";
        }

        model.addAttribute("materiaPrima", materiaPrima);
        return "materiesPrimeres/formMateriesPrimeres";
    }


    // ACTUALITZAR MATÈRIA PRIMERA
    @PostMapping("/update/{id}")
    public String updateMateriaPrima(@PathVariable Long id,
                                     @ModelAttribute("materiaPrima") MateriaPrima materiaPrima,
                                     Model model) {
        try {
            materiaPrimaService.updateMateriaPrima(id, materiaPrima);
            return "redirect:/materies-primeres/list";
        }
        catch (RuntimeException e) {
            model.addAttribute("error", e.getMessage());
            model.addAttribute("materiaPrima", materiaPrima);
            return "materiesPrimeres/formMateriesPrimeres";
        }
    }


    // ELIMINAR MATÈRIA PRIMERA
    @GetMapping("/delete/{id}")
    public String deleteMateriaPrima(@PathVariable Long id) {
        materiaPrimaService.deleteMateriaPrima(id);
        return "redirect:/materies-primeres/list";
    }
}