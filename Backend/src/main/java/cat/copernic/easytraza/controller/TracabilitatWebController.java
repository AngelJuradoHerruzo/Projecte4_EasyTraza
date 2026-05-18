package cat.copernic.easytraza.controller;

import java.time.LocalDate;
import java.util.ArrayList;

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

    // RF20 + RF21 + RF22 - EXPLOTACIÓ DE DADES
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
                                      Model model) {
        try {
            carregarModelBase(model, materiaId, proveidorId, estat, identificadorLot,
                    dataRecepcio, lotId, buscar, sortField, sortDir);

            return "tracabilitat/llistatPerLot";
        }
        catch (RuntimeException e) {
            model.addAttribute("error", e.getMessage());

            carregarModelBase(model, materiaId, proveidorId, estat, identificadorLot,
                    dataRecepcio, null, buscar, sortField, sortDir);

            return "tracabilitat/llistatPerLot";
        }
    }

    // CARREGAR MODEL
    private void carregarModelBase(Model model,
                                   Long materiaId,
                                   Long proveidorId,
                                   EstatLot estat,
                                   String identificadorLot,
                                   LocalDate dataRecepcio,
                                   Long lotId,
                                   Boolean buscar,
                                   String sortField,
                                   String sortDir) {

        boolean cercaRealitzada = Boolean.TRUE.equals(buscar);

        if (sortField == null || sortField.isBlank()) {
            sortField = "id";
        }

        if (sortDir == null || sortDir.isBlank()) {
            sortDir = "asc";
        }

        model.addAttribute("materiesPrimeres", tracabilitatService.getAllMateriesPrimeresOrdenades());
        model.addAttribute("proveidors", tracabilitatService.getAllProveidorsOrdenats());
        model.addAttribute("estats", tracabilitatService.getEstatsLot());

        if (cercaRealitzada) {
            model.addAttribute("lots", tracabilitatService.getLotsFiltrats(
                    materiaId,
                    proveidorId,
                    estat,
                    identificadorLot,
                    dataRecepcio,
                    sortField,
                    sortDir
            ));
        } 
        else {
            model.addAttribute("lots", new ArrayList<>());
        }

        model.addAttribute("buscar", cercaRealitzada);

        model.addAttribute("materiaId", materiaId);
        model.addAttribute("proveidorId", proveidorId);
        model.addAttribute("estat", estat);
        model.addAttribute("identificadorLot", identificadorLot);
        model.addAttribute("dataRecepcio", dataRecepcio);
        model.addAttribute("lotId", lotId);

        model.addAttribute("sortField", sortField);
        model.addAttribute("sortDir", sortDir);

        model.addAttribute("lotSeleccionat", tracabilitatService.getLotById(lotId));
        model.addAttribute("liniesProduccio", tracabilitatService.getProduccioPerLot(lotId));
    }
}