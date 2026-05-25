package cat.copernic.easytraza.controller;

import java.time.LocalDate;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import cat.copernic.easytraza.enums.EstatLot;
import cat.copernic.easytraza.service.TracabilitatService;

@Controller
@RequestMapping("/tracabilitat")
public class TracabilitatWebController {

    // ---------------------------- SERVICE I CONSTRUCTOR ----------------------------
    private final TracabilitatService tracabilitatService;

    public TracabilitatWebController(TracabilitatService tracabilitatService) {
        this.tracabilitatService = tracabilitatService;
    }

    // EXPLOTACIÓ DE DADES
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
