package cat.copernic.easytraza.controller;

import java.time.LocalDate;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import cat.copernic.easytraza.enums.EstatLot;
import cat.copernic.easytraza.service.TracabilitatService;

/**
 * CONTROLADOR WEB DE TRAÇABILITAT.
 *
 * Gestionada la pantalla d'explotació de dades dels lots i la consulta
 * de la informació necessària per al seguiment de la traçabilitat.
 *
 * @author Ángel Jurado Herruz
 */
@Controller
@RequestMapping("/tracabilitat")
public class TracabilitatWebController {

    private static final Logger LOGGER = LoggerFactory.getLogger(TracabilitatWebController.class);

    // ---------------------------- SERVICE I CONSTRUCTOR ----------------------------
    private final TracabilitatService tracabilitatService;

    public TracabilitatWebController(TracabilitatService tracabilitatService) {
        this.tracabilitatService = tracabilitatService;
    }


    /**
     * EXPLOTACIÓ DE DADES DE TRAÇABILITAT.
     *
     * Preparada la vista de traçabilitat amb els filtres del llistat de lots
     * i els criteris utilitzats per mostrar la informació gràfica.
     *
     * @param materiaId identificador de la matèria primera utilitzat com a filtre
     * @param proveidorId identificador del proveïdor utilitzat com a filtre
     * @param estat estat del lot utilitzat com a filtre
     * @param identificadorLot identificador del lot utilitzat com a filtre
     * @param dataRecepcio data de recepció utilitzada com a filtre
     * @param lotId identificador del lot seleccionat per consultar el detall
     * @param buscar indicador d'aplicació dels criteris de cerca
     * @param sortField camp utilitzat per ordenar el llistat
     * @param sortDir sentit de l'ordenació aplicada
     * @param producteGraficId identificador del producte seleccionat al gràfic
     * @param mesGrafic mes seleccionat per al gràfic
     * @param model model de dades de la vista
     * @return vista d'explotació de dades de traçabilitat
     */
    @GetMapping("/list")
    public String llistarTracabilitat(@RequestParam(required = false) Long materiaId,
                                      @RequestParam(required = false) Long proveidorId,
                                      @RequestParam(required = false) EstatLot estat,
                                      @RequestParam(required = false) String identificadorLot,
                                      @RequestParam(required = false) LocalDate dataRecepcio,
                                      @RequestParam(required = false) Long lotId,
                                      @RequestParam(required = false) Boolean buscar,
                                      @RequestParam(required = false) String sortField,
                                      @RequestParam(required = false) String sortDir,
                                      @RequestParam(required = false) Long producteGraficId,
                                      @RequestParam(required = false) String mesGrafic,
                                      Model model) {
        try {
            model.addAllAttributes(tracabilitatService.getModelTracabilitat(
                    materiaId,
                    proveidorId,
                    estat,
                    identificadorLot,
                    dataRecepcio,
                    lotId,
                    buscar,
                    sortField,
                    sortDir,
                    producteGraficId,
                    mesGrafic
            ));

            return "tracabilitat/llistatPerLot";
        }
        catch (RuntimeException e) {
            LOGGER.warn("No s'han pogut carregar les dades de traçabilitat: {}", e.getMessage());
            model.addAttribute("error", e.getMessage());

            model.addAllAttributes(tracabilitatService.getModelTracabilitat(
                    materiaId,
                    proveidorId,
                    estat,
                    identificadorLot,
                    dataRecepcio,
                    null,
                    buscar,
                    sortField,
                    sortDir,
                    producteGraficId,
                    mesGrafic
            ));

            return "tracabilitat/llistatPerLot";
        }
    }
}
