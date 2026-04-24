package cat.copernic.easytraza.controller;


import cat.copernic.easytraza.enums.EstatLot;
import cat.copernic.easytraza.service.LotProveidorService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/lots")
public class LotProveidorWebController {

    // ---------------------------- SERVICE ----------------------------
    private final LotProveidorService lotProveidorService;

    public LotProveidorWebController(LotProveidorService lotProveidorService) {
        this.lotProveidorService = lotProveidorService;
    }


    // ---------------------------- LLISTAR ----------------------------
    @GetMapping
    public String list(Model model) {
        model.addAttribute("lots", lotProveidorService.getAllLotsProveidor());
        return "lots/llistarLots";
    }


    // ---------------------------- CANVIAR ESTAT ----------------------------
    @PostMapping("/estat/{id}")
    public String canviarEstat(@PathVariable Long id,
                               @RequestParam EstatLot estat) {

        lotProveidorService.canviarEstatLot(id, estat);
        return "redirect:/lots";
    }
}