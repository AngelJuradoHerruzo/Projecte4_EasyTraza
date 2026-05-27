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

/**
 * CONTROLADOR WEB DE PROVEÏDORS.
 *
 * Gestionades les pantalles de consulta, creació, edició i eliminació
 * dels proveïdors de l'aplicació web.
 *
 * @author Ángel Jurado Herruz
 */
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


    /**
     * LLISTAT DE PROVEÏDORS.
     *
     * Preparada la vista amb els proveïdors filtrats i ordenats segons
     * els criteris indicats.
     *
     * @param nomProveidor nom del proveïdor utilitzat com a filtre
     * @param cif CIF del proveïdor utilitzat com a filtre
     * @param ordre camp utilitzat per ordenar el llistat
     * @param direccio sentit de l'ordenació aplicada
     * @param model model de dades de la vista
     * @return vista del llistat de proveïdors
     */
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


    /**
     * FORMULARI DE CREACIÓ DE PROVEÏDOR.
     *
     * Preparat el formulari necessari per donar d'alta un proveïdor nou,
     * també quan s'obre des d'una finestra emergent.
     *
     * @param popup indicador d'obertura del formulari en finestra emergent
     * @param model model de dades de la vista
     * @return vista del formulari de proveïdors
     */
    @GetMapping("/new")
    public String formCrearProveidor(@RequestParam(value = "popup", defaultValue = "false") boolean popup,
                                     Model model) {
        model.addAttribute("proveidor", new Proveidor());
        model.addAttribute("popup", popup);
        return "proveidors/formProveidors";
    }


    /**
     * GUARDAT D'UN PROVEÏDOR.
     *
     * Processat el formulari de creació d'un proveïdor i mostrats
     * els missatges corresponents segons el resultat de l'operació.
     *
     * @param proveidor dades del proveïdor introduïdes al formulari
     * @param popup indicador d'obertura del formulari en finestra emergent
     * @param model model de dades de la vista
     * @param redirectAttributes atributs mostrats després de la redirecció
     * @return redirecció al llistat o vista del formulari amb errors
     */
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


    /**
     * FORMULARI D'EDICIÓ DE PROVEÏDOR.
     *
     * Carregat el proveïdor seleccionat per mostrar-ne les dades
     * al formulari d'edició.
     *
     * @param id identificador del proveïdor
     * @param popup indicador d'obertura del formulari en finestra emergent
     * @param model model de dades de la vista
     * @return vista del formulari o redirecció al llistat
     */
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


    /**
     * ACTUALITZACIÓ D'UN PROVEÏDOR.
     *
     * Processats els canvis del proveïdor seleccionat i gestionats
     * els missatges derivats del resultat de l'operació.
     *
     * @param id identificador del proveïdor
     * @param proveidor dades actualitzades del proveïdor
     * @param popup indicador d'obertura del formulari en finestra emergent
     * @param model model de dades de la vista
     * @param redirectAttributes atributs mostrats després de la redirecció
     * @return redirecció al llistat o vista del formulari amb errors
     */
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


    /**
     * ELIMINACIÓ D'UN PROVEÏDOR.
     *
     * Sol·licitada l'eliminació del proveïdor seleccionat i informat l'usuari
     * del resultat de l'operació.
     *
     * @param id identificador del proveïdor
     * @param redirectAttributes atributs mostrats després de la redirecció
     * @return redirecció al llistat de proveïdors
     */
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
