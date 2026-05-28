package cat.copernic.easytraza.controller;

import java.time.LocalDate;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import cat.copernic.easytraza.service.LotProveidorService;

/**
 * CONTROLADOR WEB DE LOTS DE PROVEÏDOR.
 *
 * Gestionades les pantalles de consulta, inici i finalització dels lots
 * de proveïdor de la interfície web.
 *
 * @author Ángel Jurado Herruzo
 */
@Controller
@RequestMapping("/lots")
public class LotProveidorWebController {

    private static final Logger LOGGER = LoggerFactory.getLogger(LotProveidorWebController.class);

    // ---------------------------- SERVICE I CONSTRUCTOR ----------------------------
    private final LotProveidorService lotProveidorService;

    public LotProveidorWebController(LotProveidorService lotProveidorService) {
        this.lotProveidorService = lotProveidorService;
    }


    /**
     * LLISTAT DE LOTS DE PROVEÏDOR.
     *
     * Preparada la vista amb els lots filtrats segons els criteris rebuts
     * des de la pantalla web.
     *
     * @param materiaId identificador de la matèria primera utilitzat com a filtre
     * @param identificadorLot identificador del lot utilitzat com a filtre
     * @param dataCaducitat data de caducitat utilitzada com a filtre
     * @param dataRecepcio data de recepció utilitzada com a filtre
     * @param dataObertura data d'obertura utilitzada com a filtre
     * @param dataAcabament data d'acabament utilitzada com a filtre
     * @param model model de dades de la vista
     * @return vista del llistat de lots
     */
    @GetMapping("/list")
    public String llistarLots(@RequestParam(required = false) Long materiaId,
                              @RequestParam(required = false) String identificadorLot,
                              @RequestParam(required = false) LocalDate dataCaducitat,
                              @RequestParam(required = false) LocalDate dataObertura,
                              @RequestParam(required = false) LocalDate dataAcabament,
                              Model model) {

        model.addAttribute("lots", lotProveidorService.getLotsProveidorLlistat(
                materiaId, identificadorLot, dataCaducitat, dataObertura, dataAcabament
        ));
        model.addAttribute("materiesPrimeres", lotProveidorService.getAllMateriesPrimeresOrdenades());
        model.addAttribute("materiaId", materiaId);
        model.addAttribute("identificadorLot", identificadorLot);
        model.addAttribute("dataCaducitat", dataCaducitat);
        model.addAttribute("dataObertura", dataObertura);
        model.addAttribute("dataAcabament", dataAcabament);

        return "lots/llistarLots";
    }


    /**
     * DETALL D'UN LOT DE PROVEÏDOR.
     *
     * Carregades les dades del lot seleccionat per mostrar-ne el detall
     * o redirigida la petició quan no existeix.
     *
     * @param id identificador del lot de proveïdor
     * @param model model de dades de la vista
     * @return vista de detall o redirecció al llistat
     */
    @GetMapping("/detail/{id}")
    public String consultarLot(@PathVariable Long id, Model model) {
        try {
            model.addAttribute("lot", lotProveidorService.getLotProveidorById(id));
            return "lots/detallLot";
        }
        catch (RuntimeException e) {
            LOGGER.warn("No s'ha pogut consultar el lot amb identificador {}: {}", id, e.getMessage());
            model.addAttribute("error", e.getMessage());
            model.addAttribute("lots", lotProveidorService.getLotsProveidorLlistat(null, null, null, null, null));
            model.addAttribute("materiesPrimeres", lotProveidorService.getAllMateriesPrimeresOrdenades());

            return "lots/llistarLots";
        }
    }


    /**
     * INICI D'UN LOT DE PROVEÏDOR.
     *
     * Sol·licitat l'inici del lot seleccionat i preparada la confirmació
     * quan l'operació ho requereix.
     *
     * @param id identificador del lot de proveïdor
     * @param model model de dades de la vista
     * @return redirecció o vista corresponent al resultat de l'operació
     */
    @PostMapping("/iniciar/{id}")
    public String iniciarLot(@PathVariable Long id, Model model) {
        try {
            if (lotProveidorService.existeixLotObertMateixaMateria(id)) {
                model.addAttribute("confirmarIniciLot", true);
                model.addAttribute("lotAIniciar", lotProveidorService.getLotProveidorById(id));
                model.addAttribute("lots", lotProveidorService.getLotsProveidorLlistat(null, null, null, null, null));
                model.addAttribute("materiesPrimeres", lotProveidorService.getAllMateriesPrimeresOrdenades());

                return "lots/llistarLots";
            }

            lotProveidorService.iniciarLot(id, false);
            LOGGER.info("Lot amb identificador {} iniciat correctament.", id);
            return "redirect:/lots/list";
        }
        catch (RuntimeException e) {
            LOGGER.warn("No s'ha pogut iniciar el lot amb identificador {}: {}", id, e.getMessage());
            model.addAttribute("error", e.getMessage());
            model.addAttribute("lots", lotProveidorService.getLotsProveidorLlistat(null, null, null, null, null));
            model.addAttribute("materiesPrimeres", lotProveidorService.getAllMateriesPrimeresOrdenades());

            return "lots/llistarLots";
        }
    }


    /**
     * CONFIRMACIÓ DE L'INICI D'UN LOT.
     *
     * Confirmat l'inici del lot seleccionat després de validar
     * l'operació requerida per l'usuari.
     *
     * @param id identificador del lot de proveïdor
     * @param model model de dades de la vista
     * @return redirecció o vista corresponent al resultat de l'operació
     */
    @PostMapping("/iniciar-confirmat/{id}")
    public String confirmarIniciLot(@PathVariable Long id, Model model) {
        try {
            lotProveidorService.iniciarLot(id, true);
            LOGGER.info("Inici del lot amb identificador {} confirmat correctament.", id);
            return "redirect:/lots/list";
        }
        catch (RuntimeException e) {
            LOGGER.warn("No s'ha pogut confirmar l'inici del lot amb identificador {}: {}", id, e.getMessage());
            model.addAttribute("error", e.getMessage());
            model.addAttribute("lots", lotProveidorService.getLotsProveidorLlistat(null, null, null, null, null));
            model.addAttribute("materiesPrimeres", lotProveidorService.getAllMateriesPrimeresOrdenades());

            return "lots/llistarLots";
        }
    }


    /**
     * FINALITZACIÓ D'UN LOT DE PROVEÏDOR.
     *
     * Sol·licitada la finalització del lot seleccionat i informat
     * l'usuari del resultat de l'operació.
     *
     * @param id identificador del lot de proveïdor
     * @param model model de dades de la vista
     * @return redirecció o vista corresponent al resultat de l'operació
     */
    @PostMapping("/finalitzar/{id}")
    public String finalitzarLot(@PathVariable Long id, Model model) {
        try {
            lotProveidorService.finalitzarLot(id);
            LOGGER.info("Lot amb identificador {} finalitzat correctament.", id);
            return "redirect:/lots/list";
        }
        catch (RuntimeException e) {
            LOGGER.warn("No s'ha pogut finalitzar el lot amb identificador {}: {}", id, e.getMessage());
            model.addAttribute("error", e.getMessage());
            model.addAttribute("lots", lotProveidorService.getLotsProveidorLlistat(null, null, null, null, null));
            model.addAttribute("materiesPrimeres", lotProveidorService.getAllMateriesPrimeresOrdenades());

            return "lots/llistarLots";
        }
    }
}
