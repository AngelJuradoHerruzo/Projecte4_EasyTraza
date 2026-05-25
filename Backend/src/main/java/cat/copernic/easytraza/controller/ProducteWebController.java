package cat.copernic.easytraza.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import cat.copernic.easytraza.entities.Producte;
import cat.copernic.easytraza.service.ProducteService;

@Controller
@RequestMapping("/productes")
public class ProducteWebController {

    // ---------------------------- SERVICE I CONSTRUCTOR ----------------------------
    private final ProducteService producteService;

    public ProducteWebController(ProducteService producteService) {
        this.producteService = producteService;
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

            redirectAttributes.addFlashAttribute(
                "missatge",
                "El producte s'ha creat correctament."
            );

            return "redirect:/productes/list";
        }
        catch (RuntimeException e) {
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

            if (producteActualitzat == null) {
                redirectAttributes.addFlashAttribute(
                    "error",
                    "No s'ha trobat el producte que vols modificar."
                );

                return "redirect:/productes/list";
            }

            redirectAttributes.addFlashAttribute(
                "missatge",
                "El producte s'ha actualitzat correctament."
            );

            return "redirect:/productes/list";
        }
        catch (RuntimeException e) {
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

            redirectAttributes.addFlashAttribute(
                "missatge",
                "El producte s'ha eliminat correctament."
            );
        }
        catch (RuntimeException e) {
            redirectAttributes.addFlashAttribute(
                "error",
                "No es pot eliminar el producte perquè està relacionat amb altres dades."
            );
        }

        return "redirect:/productes/list";
    }
}
