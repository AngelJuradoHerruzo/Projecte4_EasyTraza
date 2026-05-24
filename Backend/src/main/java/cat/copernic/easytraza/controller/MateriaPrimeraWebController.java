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
    public String llistarMateriesPrimeres(@RequestParam(required = false) String nomMateria,
                                          Model model) {

        model.addAttribute("materiesPrimeres", materiaPrimeraService.getMateriesPrimeresLlistat(nomMateria));
        model.addAttribute("nomMateria", nomMateria);

        return "materiesPrimeres/llistarMateriesPrimeres";
    }


    // FORMULARI CREAR MATÈRIA PRIMERA
    @GetMapping("/new")
    public String formCrearMateriaPrimera(@RequestParam(value = "popup", defaultValue = "false") boolean popup,
                                          Model model) {
        model.addAttribute("materiaPrimera", new MateriaPrimera());
        model.addAttribute("popup", popup);
        return "materiesPrimeres/formMateriesPrimeres";
    }


    // GUARDAR MATÈRIA PRIMERA
    @PostMapping("/save")
    public String guardarMateriaPrimera(@ModelAttribute("materiaPrimera") MateriaPrimera materiaPrimera,
                                        @RequestParam(value = "popup", defaultValue = "false") boolean popup,
                                        Model model) {
        try {
            materiaPrimeraService.createMateriaPrimera(materiaPrimera);

            if (popup) {
                model.addAttribute("missatge", "Matèria primera creada correctament.");
                return "layout/tancarFinestra";
            }

            return "redirect:/materies-primeres/list";
        }
        catch (RuntimeException e) {
            model.addAttribute("error", e.getMessage());
            model.addAttribute("materiaPrimera", materiaPrimera);
            model.addAttribute("popup", popup);
            return "materiesPrimeres/formMateriesPrimeres";
        }
    }


    // FORMULARI EDITAR MATÈRIA PRIMERA
    @GetMapping("/edit/{id}")
    public String formEditarMateriaPrimera(@PathVariable Long id,
                                           @RequestParam(value = "popup", defaultValue = "false") boolean popup,
                                           Model model) {
        MateriaPrimera materiaPrimera = materiaPrimeraService.getMateriaPrimeraById(id);

        if (materiaPrimera == null) {
            return "redirect:/materies-primeres/list";
        }

        model.addAttribute("materiaPrimera", materiaPrimera);
        model.addAttribute("popup", popup);
        return "materiesPrimeres/formMateriesPrimeres";
    }


    // ACTUALITZAR MATÈRIA PRIMERA
    @PostMapping("/update/{id}")
    public String updateMateriaPrimera(@PathVariable Long id,
                                       @ModelAttribute("materiaPrimera") MateriaPrimera materiaPrimera,
                                       @RequestParam(value = "popup", defaultValue = "false") boolean popup,
                                       Model model) {
        try {
            materiaPrimeraService.updateMateriaPrimera(id, materiaPrimera);

            if (popup) {
                model.addAttribute("missatge", "Matèria primera actualitzada correctament.");
                return "layout/tancarFinestra";
            }

            return "redirect:/materies-primeres/list";
        }
        catch (RuntimeException e) {
            model.addAttribute("error", e.getMessage());
            model.addAttribute("materiaPrimera", materiaPrimera);
            model.addAttribute("popup", popup);
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
