package cat.copernic.easytraza.controller;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import cat.copernic.easytraza.entities.MateriaPrimera;
import cat.copernic.easytraza.service.MateriaPrimeraService;

/**
 * CONTROLADOR WEB DE MATÈRIES PRIMERES.
 *
 * Gestionades les pantalles de consulta, creació, edició i eliminació
 * de les matèries primeres de l'aplicació web.
 *
 * @author Ángel Jurado Herruzo
 */
@Controller
@RequestMapping("/materies-primeres")
public class MateriaPrimeraWebController {

    private static final Logger LOGGER = LoggerFactory.getLogger(MateriaPrimeraWebController.class);

    // ---------------------------- SERVICE I CONSTRUCTOR ----------------------------
    private final MateriaPrimeraService materiaPrimeraService;
    private final MessageSource messageSource;

    public MateriaPrimeraWebController(MateriaPrimeraService materiaPrimeraService, MessageSource messageSource) {
        this.materiaPrimeraService = materiaPrimeraService;
        this.messageSource = messageSource;
    }


    /**
     * LLISTAT DE MATÈRIES PRIMERES.
     *
     * Preparada la vista amb les matèries primeres filtrades i ordenades
     * segons els criteris indicats.
     *
     * @param nomMateria nom de la matèria primera utilitzat com a filtre
     * @param ordre camp utilitzat per ordenar el llistat
     * @param direccio sentit de l'ordenació aplicada
     * @param model model de dades de la vista
     * @return vista del llistat de matèries primeres
     */
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


    /**
     * FORMULARI DE CREACIÓ DE MATÈRIA PRIMERA.
     *
     * Preparat el formulari necessari per donar d'alta una nova matèria
     * primera, també quan s'obre des d'una finestra emergent.
     *
     * @param popup indicador d'obertura del formulari en finestra emergent
     * @param model model de dades de la vista
     * @return vista del formulari de matèries primeres
     */
    @GetMapping("/new")
    public String formCrearMateriaPrimera(@RequestParam(value = "popup", defaultValue = "false") boolean popup,
                                          Model model) {
        model.addAttribute("materiaPrimera", new MateriaPrimera());
        model.addAttribute("popup", popup);
        return "materiesPrimeres/formMateriesPrimeres";
    }


    /**
     * GUARDAT D'UNA MATÈRIA PRIMERA.
     *
     * Processat el formulari de creació d'una matèria primera i mostrats
     * els missatges corresponents segons el resultat de l'operació.
     *
     * @param materiaPrimera dades introduïdes al formulari
     * @param popup indicador d'obertura del formulari en finestra emergent
     * @param model model de dades de la vista
     * @param redirectAttributes atributs mostrats després de la redirecció
     * @return redirecció al llistat o vista del formulari amb errors
     */
    @PostMapping("/save")
    public String guardarMateriaPrimera(@ModelAttribute("materiaPrimera") MateriaPrimera materiaPrimera,
                                        @RequestParam(value = "popup", defaultValue = "false") boolean popup,
                                        Model model,
                                        RedirectAttributes redirectAttributes) {
        try {
            materiaPrimeraService.createMateriaPrimera(materiaPrimera);
            LOGGER.info("Matèria primera creada correctament.");

            if (popup) {
                model.addAttribute("missatge", missatge("materiesPrimeres.missatge.creadaPopup"));
                return "layout/tancarFinestra";
            }

            redirectAttributes.addFlashAttribute(
                "missatge",
                missatge("materiesPrimeres.missatge.creada")
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


    /**
     * FORMULARI D'EDICIÓ DE MATÈRIA PRIMERA.
     *
     * Carregada la matèria primera seleccionada per mostrar-ne les dades
     * al formulari d'edició.
     *
     * @param id identificador de la matèria primera
     * @param popup indicador d'obertura del formulari en finestra emergent
     * @param model model de dades de la vista
     * @return vista del formulari o redirecció al llistat
     */
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


    /**
     * ACTUALITZACIÓ D'UNA MATÈRIA PRIMERA.
     *
     * Processats els canvis de la matèria primera seleccionada i gestionats
     * els missatges derivats del resultat de l'operació.
     *
     * @param id identificador de la matèria primera
     * @param materiaPrimera dades actualitzades de la matèria primera
     * @param popup indicador d'obertura del formulari en finestra emergent
     * @param model model de dades de la vista
     * @param redirectAttributes atributs mostrats després de la redirecció
     * @return redirecció al llistat o vista del formulari amb errors
     */
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
                    missatge("materiesPrimeres.error.noTrobadaModificar")
                );

                return "redirect:/materies-primeres/list";
            }

            if (popup) {
                model.addAttribute("missatge", missatge("materiesPrimeres.missatge.actualitzadaPopup"));
                return "layout/tancarFinestra";
            }

            redirectAttributes.addFlashAttribute(
                "missatge",
                missatge("materiesPrimeres.missatge.actualitzada")
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


    /**
     * ELIMINACIÓ D'UNA MATÈRIA PRIMERA.
     *
     * Sol·licitada l'eliminació de la matèria primera seleccionada i informat
     * l'usuari del resultat de l'operació.
     *
     * @param id identificador de la matèria primera
     * @param redirectAttributes atributs mostrats després de la redirecció
     * @return redirecció al llistat de matèries primeres
     */
    @PostMapping("/delete/{id}")
    public String deleteMateriaPrimera(@PathVariable Long id,
                                       RedirectAttributes redirectAttributes) {
        try {
            materiaPrimeraService.deleteMateriaPrimera(id);
            LOGGER.info("Matèria primera amb identificador {} eliminada correctament.", id);

            redirectAttributes.addFlashAttribute(
                "missatge",
                missatge("materiesPrimeres.missatge.eliminada")
            );
        }
        catch (RuntimeException e) {
            LOGGER.warn("No s'ha pogut eliminar la matèria primera amb identificador {}: {}", id, e.getMessage());
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }

        return "redirect:/materies-primeres/list";
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
