package cat.copernic.easytraza.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
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
    private final MessageSource messageSource;

    public ProveidorWebController(ProveidorService proveidorService, MessageSource messageSource) {
        this.proveidorService = proveidorService;
        this.messageSource = messageSource;
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
                model.addAttribute("missatge", missatge("proveidors.missatge.creatPopup"));
                return "layout/tancarFinestra";
            }

            redirectAttributes.addFlashAttribute(
                "missatge",
                missatge("proveidors.missatge.creat")
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
                    missatge("proveidors.error.noTrobatModificar")
                );

                return "redirect:/proveidors/list";
            }

            if (popup) {
                model.addAttribute("missatge", missatge("proveidors.missatge.actualitzatPopup"));
                return "layout/tancarFinestra";
            }

            redirectAttributes.addFlashAttribute(
                "missatge",
                missatge("proveidors.missatge.actualitzat")
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
                missatge("proveidors.missatge.eliminat")
            );
        }
        catch (RuntimeException e) {
            LOGGER.warn("No s'ha pogut eliminar el proveïdor amb identificador {}: {}", id, e.getMessage());
            redirectAttributes.addFlashAttribute(
                "error",
                missatge("proveidors.error.eliminarRelacionat")
            );
        }

        return "redirect:/proveidors/list";
    }

    private String missatge(String codi, Object... arguments) {
        return messageSource.getMessage(codi, arguments, LocaleContextHolder.getLocale());
    }

}
