package cat.copernic.easytraza.controller;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import cat.copernic.easytraza.entities.MateriaPrimera;
import cat.copernic.easytraza.service.MateriaPrimeraService;

@Controller
@RequestMapping("/materies-primeres")
public class MateriaPrimeraWebController {

    private static final Logger LOGGER = LoggerFactory.getLogger(MateriaPrimeraWebController.class);

    // ---------------------------- SERVICE I CONSTRUCTOR ----------------------------
    private final MateriaPrimeraService materiaPrimeraService;

    public MateriaPrimeraWebController(MateriaPrimeraService materiaPrimeraService) {
        this.materiaPrimeraService = materiaPrimeraService;
    }


    // LLISTAR MATÈRIES PRIMERES
    @GetMapping("/list")
    public String llistarMateriesPrimeres(@RequestParam(required = false) String nomMateria,
                                          @RequestParam(required = false, defaultValue = "nomMateria") String ordre,
                                          @RequestParam(required = false, defaultValue = "asc") String direccio,
                                          Model model) {

        List<MateriaPrimera> materiesPrimeres = materiaPrimeraService.getMateriesPrimeresLlistat(
            nomMateria,
            ordre,
            direccio
        );

        model.addAttribute("materiesPrimeres", materiesPrimeres);
        model.addAttribute("materiesEnUs", materiaPrimeraService.getIdsMateriesPrimeresEnUs(materiesPrimeres));
        model.addAttribute("nomMateria", nomMateria);
        model.addAttribute("ordre", ordre);
        model.addAttribute("direccio", direccio);

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
                                        Model model,
                                        RedirectAttributes redirectAttributes) {
        try {
            materiaPrimeraService.createMateriaPrimera(materiaPrimera);
            LOGGER.info("Matèria primera creada correctament.");

            if (popup) {
                model.addAttribute("missatge", "Matèria primera creada correctament.");
                return "layout/tancarFinestra";
            }

            redirectAttributes.addFlashAttribute(
                "missatge",
                "La matèria primera s'ha creat correctament."
            );

            return "redirect:/materies-primeres/list";
        }
        catch (RuntimeException e) {
            LOGGER.warn("No s'ha pogut crear la matèria primera: {}", e.getMessage());
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
                                       Model model,
                                       RedirectAttributes redirectAttributes) {
        try {
            MateriaPrimera materiaActualitzada = materiaPrimeraService.updateMateriaPrimera(id, materiaPrimera);
            LOGGER.info("Matèria primera amb identificador {} actualitzada correctament.", id);

            if (materiaActualitzada == null) {
                redirectAttributes.addFlashAttribute(
                    "error",
                    "No s'ha trobat la matèria primera que vols modificar."
                );

                return "redirect:/materies-primeres/list";
            }

            if (popup) {
                model.addAttribute("missatge", "Matèria primera actualitzada correctament.");
                return "layout/tancarFinestra";
            }

            redirectAttributes.addFlashAttribute(
                "missatge",
                "La matèria primera s'ha actualitzat correctament."
            );

            return "redirect:/materies-primeres/list";
        }
        catch (RuntimeException e) {
            LOGGER.warn("No s'ha pogut actualitzar la matèria primera amb identificador {}: {}", id, e.getMessage());
            materiaPrimera.setId(id);
            model.addAttribute("error", e.getMessage());
            model.addAttribute("materiaPrimera", materiaPrimera);
            model.addAttribute("popup", popup);
            return "materiesPrimeres/formMateriesPrimeres";
        }
    }


    // ELIMINAR MATÈRIA PRIMERA
    @PostMapping("/delete/{id}")
    public String deleteMateriaPrimera(@PathVariable Long id,
                                       RedirectAttributes redirectAttributes) {
        try {
            materiaPrimeraService.deleteMateriaPrimera(id);
            LOGGER.info("Matèria primera amb identificador {} eliminada correctament.", id);

            redirectAttributes.addFlashAttribute(
                "missatge",
                "La matèria primera s'ha eliminat correctament."
            );
        }
        catch (RuntimeException e) {
            LOGGER.warn("No s'ha pogut eliminar la matèria primera amb identificador {}: {}", id, e.getMessage());
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }

        return "redirect:/materies-primeres/list";
    }
}
