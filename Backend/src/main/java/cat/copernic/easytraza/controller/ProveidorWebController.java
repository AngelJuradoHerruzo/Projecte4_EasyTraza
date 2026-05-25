package cat.copernic.easytraza.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import cat.copernic.easytraza.entities.Proveidor;
import cat.copernic.easytraza.service.ProveidorService;

@Controller
@RequestMapping("/proveidors")
public class ProveidorWebController {

    private static final Logger LOGGER = LoggerFactory.getLogger(ProveidorWebController.class);

    // ---------------------------- SERVICE I CONSTRUCTOR ----------------------------
    private final ProveidorService proveidorService;

    public ProveidorWebController(ProveidorService proveidorService) {
        this.proveidorService = proveidorService;
    }


    // LLISTAR PROVEÏDORS
    @GetMapping("/list")
    public String llistarProveidors(@RequestParam(required = false) String nomProveidor,
                                    @RequestParam(required = false) String cif,
                                    @RequestParam(required = false, defaultValue = "nomProveidor") String ordre,
                                    @RequestParam(required = false, defaultValue = "asc") String direccio,
                                    Model model) {

        model.addAttribute(
            "proveidors",
            proveidorService.getProveidorsLlistat(nomProveidor, cif, ordre, direccio)
        );
        model.addAttribute("nomProveidor", nomProveidor);
        model.addAttribute("cif", cif);
        model.addAttribute("ordre", ordre);
        model.addAttribute("direccio", direccio);

        return "proveidors/llistarProveidors";
    }


    // FORMULARI CREAR PROVEÏDOR
    @GetMapping("/new")
    public String formCrearProveidor(@RequestParam(value = "popup", defaultValue = "false") boolean popup,
                                     Model model) {
        model.addAttribute("proveidor", new Proveidor());
        model.addAttribute("popup", popup);
        return "proveidors/formProveidors";
    }


    // GUARDAR PROVEÏDOR
    @PostMapping("/save")
    public String guardarProveidor(@ModelAttribute("proveidor") Proveidor proveidor,
                                   @RequestParam(value = "popup", defaultValue = "false") boolean popup,
                                   Model model,
                                   RedirectAttributes redirectAttributes) {
        try {
            proveidorService.createProveidor(proveidor);
            LOGGER.info("Proveïdor creat correctament.");

            if (popup) {
                model.addAttribute("missatge", "Proveïdor creat correctament.");
                return "layout/tancarFinestra";
            }

            redirectAttributes.addFlashAttribute(
                "missatge",
                "El proveïdor s'ha creat correctament."
            );

            return "redirect:/proveidors/list";
        }
        catch (RuntimeException e) {
            LOGGER.warn("No s'ha pogut crear el proveïdor: {}", e.getMessage());
            model.addAttribute("error", e.getMessage());
            model.addAttribute("proveidor", proveidor);
            model.addAttribute("popup", popup);
            return "proveidors/formProveidors";
        }
    }


    // FORMULARI EDITAR PROVEÏDOR
    @GetMapping("/edit/{id}")
    public String formEditarProveidor(@PathVariable Long id,
                                      @RequestParam(value = "popup", defaultValue = "false") boolean popup,
                                      Model model) {
        Proveidor proveidor = proveidorService.getProveidorById(id);

        if (proveidor == null) {
            return "redirect:/proveidors/list";
        }

        model.addAttribute("proveidor", proveidor);
        model.addAttribute("popup", popup);
        return "proveidors/formProveidors";
    }


    // ACTUALITZAR PROVEÏDOR
    @PostMapping("/update/{id}")
    public String updateProveidor(@PathVariable Long id,
                                  @ModelAttribute("proveidor") Proveidor proveidor,
                                  @RequestParam(value = "popup", defaultValue = "false") boolean popup,
                                  Model model,
                                  RedirectAttributes redirectAttributes) {
        try {
            Proveidor proveidorActualitzat = proveidorService.updateProveidor(id, proveidor);
            LOGGER.info("Proveïdor amb identificador {} actualitzat correctament.", id);

            if (proveidorActualitzat == null) {
                redirectAttributes.addFlashAttribute(
                    "error",
                    "No s'ha trobat el proveïdor que vols modificar."
                );

                return "redirect:/proveidors/list";
            }

            if (popup) {
                model.addAttribute("missatge", "Proveïdor actualitzat correctament.");
                return "layout/tancarFinestra";
            }

            redirectAttributes.addFlashAttribute(
                "missatge",
                "El proveïdor s'ha actualitzat correctament."
            );

            return "redirect:/proveidors/list";
        }
        catch (RuntimeException e) {
            LOGGER.warn("No s'ha pogut actualitzar el proveïdor amb identificador {}: {}", id, e.getMessage());
            proveidor.setId(id);
            model.addAttribute("error", e.getMessage());
            model.addAttribute("proveidor", proveidor);
            model.addAttribute("popup", popup);
            return "proveidors/formProveidors";
        }
    }


    // ELIMINAR PROVEÏDOR
    @PostMapping("/delete/{id}")
    public String deleteProveidor(@PathVariable Long id,
                                  RedirectAttributes redirectAttributes) {
        try {
            proveidorService.deleteProveidor(id);
            LOGGER.info("Proveïdor amb identificador {} eliminat correctament.", id);

            redirectAttributes.addFlashAttribute(
                "missatge",
                "El proveïdor s'ha eliminat correctament."
            );
        }
        catch (RuntimeException e) {
            LOGGER.warn("No s'ha pogut eliminar el proveïdor amb identificador {}: {}", id, e.getMessage());
            redirectAttributes.addFlashAttribute(
                "error",
                "No es pot eliminar el proveïdor perquè està relacionat amb altres dades."
            );
        }

        return "redirect:/proveidors/list";
    }
}
