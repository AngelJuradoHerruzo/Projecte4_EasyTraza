package cat.copernic.easytraza.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import cat.copernic.easytraza.entities.MateriaPrimera;
import cat.copernic.easytraza.service.MateriaPrimeraService;

@Controller
@RequestMapping("/materies-primeres")
public class MateriaPrimeraWebController {

    // ---------------------------- SERVICE I CONSTRUCTOR ----------------------------
    private final MateriaPrimeraService materiaPrimeraService;

    public MateriaPrimeraWebController(MateriaPrimeraService materiaPrimeraService) {
        this.materiaPrimeraService = materiaPrimeraService;
    }


    // LLISTAR MATÈRIES PRIMERES
    @GetMapping("/list")
    public String llistarMateriesPrimeres(Model model) {
        model.addAttribute("materiesPrimeres", materiaPrimeraService.getAllMateriesPrimeres());
        return "materiesPrimeres/llistarMateriesPrimeres";
    }


    // FORMULARI CREAR MATÈRIA PRIMERA
    @GetMapping("/new")
    public String formCrearMateriaPrimera(Model model) {
        model.addAttribute("materiaPrimera", new MateriaPrimera());
        return "materiesPrimeres/formMateriesPrimeres";
    }


    // GUARDAR MATÈRIA PRIMERA
    @PostMapping("/save")
    public String guardarMateriaPrimera(@ModelAttribute("materiaPrimera") MateriaPrimera materiaPrimera, Model model) {
        try {
            materiaPrimeraService.createMateriaPrimera(materiaPrimera);
            return "redirect:/materies-primeres/list";
        }
        catch (RuntimeException e) {
            model.addAttribute("error", e.getMessage());
            model.addAttribute("materiaPrimera", materiaPrimera);
            return "materiesPrimeres/formMateriesPrimeres";
        }
    }


    // FORMULARI EDITAR MATÈRIA PRIMERA
    @GetMapping("/edit/{id}")
    public String formEditarMateriaPrimera(@PathVariable Long id, Model model) {
        MateriaPrimera materiaPrimera = materiaPrimeraService.getMateriaPrimeraById(id);

        if (materiaPrimera == null) {
            return "redirect:/materies-primeres/list";
        }

        model.addAttribute("materiaPrimera", materiaPrimera);
        return "materiesPrimeres/formMateriesPrimeres";
    }


    // ACTUALITZAR MATÈRIA PRIMERA
    @PostMapping("/update/{id}")
    public String updateMateriaPrimera(@PathVariable Long id,
                                     @ModelAttribute("materiaPrimera") MateriaPrimera materiaPrimera,
                                     Model model) {
        try {
            materiaPrimeraService.updateMateriaPrimera(id, materiaPrimera);
            return "redirect:/materies-primeres/list";
        }
        catch (RuntimeException e) {
            model.addAttribute("error", e.getMessage());
            model.addAttribute("materiaPrimera", materiaPrimera);
            return "materiesPrimeres/formMateriesPrimeres";
        }
    }


    // ELIMINAR MATÈRIA PRIMERA
    @GetMapping("/delete/{id}")
    public String deleteMateriaPrimera(@PathVariable Long id) {
        materiaPrimeraService.deleteMateriaPrimera(id);
        return "redirect:/materies-primeres/list";
    }
}