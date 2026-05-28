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

/**
 * CONTROLADOR WEB DE PRODUCTES.
 *
 * Gestionades les pantalles de consulta, creació, edició i eliminació
 * dels productes de l'aplicació web.
 *
 * @author Ángel Jurado Herruzo
 */
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


    /**
     * LLISTAT DE PRODUCTES.
     *
     * Preparada la vista amb els productes filtrats segons el nom
     * indicat per l'usuari.
     *
     * @param nomProducte nom del producte utilitzat com a filtre
     * @param model model de dades de la vista
     * @return vista del llistat de productes
     */
    @GetMapping("/list")
    public String llistarProductes(@RequestParam(required = false) String nomProducte,
                                Model model) {

        model.addAttribute("productes", producteService.getAllProductes(nomProducte));
        model.addAttribute("nomProducte", nomProducte);

        return "productes/llistarProductes";
    }


    /**
     * FORMULARI DE CREACIÓ DE PRODUCTE.
     *
     * Preparat el formulari necessari per donar d'alta un producte nou.
     *
     * @param model model de dades de la vista
     * @return vista del formulari de productes
     */
    @GetMapping("/new")
    public String formCrearProducte(Model model) {
        model.addAttribute("producte", new Producte());
        return "productes/formProductes";
    }


    /**
     * GUARDAT D'UN PRODUCTE.
     *
     * Processat el formulari de creació d'un producte i mostrats
     * els missatges corresponents segons el resultat de l'operació.
     *
     * @param producte dades del producte introduïdes al formulari
     * @param model model de dades de la vista
     * @param redirectAttributes atributs mostrats després de la redirecció
     * @return redirecció al llistat o vista del formulari amb errors
     */
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


    /**
     * FORMULARI D'EDICIÓ DE PRODUCTE.
     *
     * Carregat el producte seleccionat per mostrar-ne les dades
     * al formulari d'edició.
     *
     * @param id identificador del producte
     * @param model model de dades de la vista
     * @return vista del formulari o redirecció al llistat
     */
    @GetMapping("/edit/{id}")
    public String formEditarProducte(@PathVariable Long id, Model model) {

        Producte producte = producteService.getProducteById(id);

        if (producte == null) {
            return "redirect:/productes/list";
        }

        model.addAttribute("producte", producte);
        return "productes/formProductes";
    }


    /**
     * ACTUALITZACIÓ D'UN PRODUCTE.
     *
     * Processats els canvis del producte seleccionat i gestionats
     * els missatges derivats del resultat de l'operació.
     *
     * @param id identificador del producte
     * @param producte dades actualitzades del producte
     * @param model model de dades de la vista
     * @param redirectAttributes atributs mostrats després de la redirecció
     * @return redirecció al llistat o vista del formulari amb errors
     */
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


    /**
     * ELIMINACIÓ D'UN PRODUCTE.
     *
     * Sol·licitada l'eliminació del producte seleccionat i informat l'usuari
     * del resultat de l'operació.
     *
     * @param id identificador del producte
     * @param redirectAttributes atributs mostrats després de la redirecció
     * @return redirecció al llistat de productes
     */
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


    /**
     * OBTENCIÓ D'UN MISSATGE TRADUÏT.
     *
     * Recuperat el text corresponent al codi indicat segons l'idioma
     * actiu de la interfície web.
     *
     * @param codi codi del missatge que s'ha de recuperar
     * @param arguments valors incorporats al missatge
     * @return missatge traduït a l'idioma actiu
     */
    private String missatge(String codi, Object... arguments) {
        return messageSource.getMessage(codi, arguments, LocaleContextHolder.getLocale());
    }

}
