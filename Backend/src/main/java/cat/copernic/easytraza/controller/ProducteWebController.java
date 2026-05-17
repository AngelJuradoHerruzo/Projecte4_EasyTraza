package cat.copernic.easytraza.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

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

        model.addAttribute("productes", producteService.getProductesLlistat(nomProducte));
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
                                 Model model) {
        try {
            producteService.createProducte(producte);
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
                                Model model) {
        try {
            producteService.updateProducte(id, producte);
            return "redirect:/productes/list";
        }
        catch (RuntimeException e) {
            model.addAttribute("error", e.getMessage());
            model.addAttribute("producte", producte);
            return "productes/formProductes";
        }
    }


    // ELIMINAR
    @GetMapping("/delete/{id}")
    public String deleteProducte(@PathVariable Long id) {
        producteService.deleteProducte(id);
        return "redirect:/productes/list";
    }
}
