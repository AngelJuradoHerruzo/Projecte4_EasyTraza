package cat.copernic.easytraza.controller;

import java.time.LocalDate;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import cat.copernic.easytraza.service.LotProveidorService;

@Controller
@RequestMapping("/lots")
public class LotProveidorWebController {

    // ---------------------------- SERVICE I CONSTRUCTOR ----------------------------
    private final LotProveidorService lotProveidorService;

    public LotProveidorWebController(LotProveidorService lotProveidorService) {
        this.lotProveidorService = lotProveidorService;
    }


    // LLISTAR LOTS DE PROVEÏDOR
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


    // CONSULTAR DETALL DEL LOT
    @GetMapping("/detail/{id}")
    public String consultarLot(@PathVariable Long id, Model model) {
        try {
            model.addAttribute("lot", lotProveidorService.getLotProveidorById(id));
            return "lots/detallLot";
        }
        catch (RuntimeException e) {
            model.addAttribute("error", e.getMessage());
            model.addAttribute("lots", lotProveidorService.getLotsProveidorLlistat(null, null, null, null, null));
            model.addAttribute("materiesPrimeres", lotProveidorService.getAllMateriesPrimeresOrdenades());

            return "lots/llistarLots";
        }
    }


    // INICIAR LOT
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
            return "redirect:/lots/list";
        }
        catch (RuntimeException e) {
            model.addAttribute("error", e.getMessage());
            model.addAttribute("lots", lotProveidorService.getLotsProveidorLlistat(null, null, null, null, null));
            model.addAttribute("materiesPrimeres", lotProveidorService.getAllMateriesPrimeresOrdenades());

            return "lots/llistarLots";
        }
    }


    // CONFIRMAR INICI DE LOT
    @PostMapping("/iniciar-confirmat/{id}")
    public String confirmarIniciLot(@PathVariable Long id, Model model) {
        try {
            lotProveidorService.iniciarLot(id, true);
            return "redirect:/lots/list";
        }
        catch (RuntimeException e) {
            model.addAttribute("error", e.getMessage());
            model.addAttribute("lots", lotProveidorService.getLotsProveidorLlistat(null, null, null, null, null));
            model.addAttribute("materiesPrimeres", lotProveidorService.getAllMateriesPrimeresOrdenades());

            return "lots/llistarLots";
        }
    }


    // FINALITZAR LOT
    @PostMapping("/finalitzar/{id}")
    public String finalitzarLot(@PathVariable Long id, Model model) {
        try {
            lotProveidorService.finalitzarLot(id);
            return "redirect:/lots/list";
        }
        catch (RuntimeException e) {
            model.addAttribute("error", e.getMessage());
            model.addAttribute("lots", lotProveidorService.getLotsProveidorLlistat(null, null, null, null, null));
            model.addAttribute("materiesPrimeres", lotProveidorService.getAllMateriesPrimeresOrdenades());

            return "lots/llistarLots";
        }
    }
}
