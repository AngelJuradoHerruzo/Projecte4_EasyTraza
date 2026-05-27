package cat.copernic.easytraza.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import cat.copernic.easytraza.entities.Producte;
import cat.copernic.easytraza.service.ProducteService;

@Controller
@RequestMapping("/productes")
public class ProducteWebController {

    private static final Logger LOGGER = LoggerFactory.getLogger(ProducteWebController.class);

    // ---------------------------- SERVICE I CONSTRUCTOR ----------------------------
    private final ProducteService producteService;
    private final MessageSource messageSource;

    public ProducteWebController(ProducteService producteService, MessageSource messageSource) {
        this.producteService = producteService;
        this.messageSource = messageSource;
    }


    // LLISTAR PRODUCTES
    @GetMapping("/list")
    public String llistarProductes(@RequestParam(required = false) String nomProducte,
                                Model model) {

        model.addAttribute("productes", producteService.getAllProductes(nomProducte));
        model.addAttribute("nomProducte", nomProducte);

        return "productes/llistarProductes";
    }


    // CREAR
    @GetMapping("/new")
    public String formCrearProducte(Model model) {
        model.addAttribute("producte", new Producte());
        return "productes/formProductes";
    }


    // GUARDAR
    @PostMapping("/save")
    public String guardarProducte(@ModelAttribute("producte") Producte producte,
                                   Model model,
                                   RedirectAttributes redirectAttributes) {
        try {
            producteService.createProducte(producte);
            LOGGER.info("Producte creat correctament.");

            redirectAttributes.addFlashAttribute(
                "missatge",
                missatge("productes.missatge.creat")
            );

            return "redirect:/productes/list";
        }
        catch (RuntimeException e) {
            LOGGER.warn("No s'ha pogut crear el producte: {}", e.getMessage());
            model.addAttribute("error", e.getMessage());
            model.addAttribute("producte", producte);
            return "productes/formProductes";
        }
    }


    // EDITAR
    @GetMapping("/edit/{id}")
    public String formEditarProducte(@PathVariable Long id, Model model) {

        Producte producte = producteService.getProducteById(id);

        if (producte == null) {
            return "redirect:/productes/list";
        }

        model.addAttribute("producte", producte);
        return "productes/formProductes";
    }


    // ACTUALITZAR
    @PostMapping("/update/{id}")
    public String updateProducte(@PathVariable Long id,
                                  @ModelAttribute("producte") Producte producte,
                                  Model model,
                                  RedirectAttributes redirectAttributes) {
        try {
            Producte producteActualitzat = producteService.updateProducte(id, producte);
            LOGGER.info("Producte amb identificador {} actualitzat correctament.", id);

            if (producteActualitzat == null) {
                redirectAttributes.addFlashAttribute(
                    "error",
                    missatge("productes.error.noTrobatModificar")
                );

                return "redirect:/productes/list";
            }

            redirectAttributes.addFlashAttribute(
                "missatge",
                missatge("productes.missatge.actualitzat")
            );

            return "redirect:/productes/list";
        }
        catch (RuntimeException e) {
            LOGGER.warn("No s'ha pogut actualitzar el producte amb identificador {}: {}", id, e.getMessage());
            model.addAttribute("error", e.getMessage());
            producte.setId(id);
            model.addAttribute("producte", producte);
            return "productes/formProductes";
        }
    }


    // ELIMINAR
    @PostMapping("/delete/{id}")
    public String deleteProducte(@PathVariable Long id,
                                  RedirectAttributes redirectAttributes) {
        try {
            producteService.deleteProducte(id);
            LOGGER.info("Producte amb identificador {} eliminat correctament.", id);

            redirectAttributes.addFlashAttribute(
                "missatge",
                missatge("productes.missatge.eliminat")
            );
        }
        catch (RuntimeException e) {
            LOGGER.warn("No s'ha pogut eliminar el producte amb identificador {}: {}", id, e.getMessage());
            redirectAttributes.addFlashAttribute(
                "error",
                missatge("productes.error.eliminarRelacionat")
            );
        }

        return "redirect:/productes/list";
    }

    private String missatge(String codi, Object... arguments) {
        return messageSource.getMessage(codi, arguments, LocaleContextHolder.getLocale());
    }

}
