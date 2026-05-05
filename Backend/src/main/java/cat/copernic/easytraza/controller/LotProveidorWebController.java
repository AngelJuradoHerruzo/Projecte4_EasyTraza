package cat.copernic.easytraza.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import cat.copernic.easytraza.enums.EstatLot;
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
    public String llistarLots(Model model) {
        model.addAttribute("lots", lotProveidorService.getAllLotsProveidor());
        model.addAttribute("estats", EstatLot.values());

        return "lots/llistarLots";
    }


    // CANVIAR ESTAT DEL LOT
    @PostMapping("/estat/{id}")
    public String canviarEstatLot(@PathVariable Long id, @RequestParam EstatLot estat, Model model) {
        try {
            lotProveidorService.canviarEstatLot(id, estat);
            return "redirect:/lots/list";
        }
        catch (RuntimeException e) {
            model.addAttribute("error", e.getMessage());
            model.addAttribute("lots", lotProveidorService.getAllLotsProveidor());
            model.addAttribute("estats", EstatLot.values());

            return "lots/llistarLots";
        }
    }
}