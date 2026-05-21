package cat.copernic.easytraza.controller;

import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import cat.copernic.easytraza.entities.AlbaraProveidor;
import cat.copernic.easytraza.entities.LotProveidor;
import cat.copernic.easytraza.entities.UnitatMesura;
import cat.copernic.easytraza.service.MateriaPrimeraService;
import cat.copernic.easytraza.service.ProveidorService;
import cat.copernic.easytraza.service.UnitatMesuraService;

@Controller
@RequestMapping("/unitats-mesura")
public class UnitatMesuraWebController {

    // ---------------------------- SERVICES I CONSTRUCTOR ----------------------------
    private final UnitatMesuraService unitatMesuraService;
    private final ProveidorService proveidorService;
    private final MateriaPrimeraService materiaPrimeraService;

    public UnitatMesuraWebController(UnitatMesuraService unitatMesuraService,
                                     ProveidorService proveidorService,
                                     MateriaPrimeraService materiaPrimeraService) {
        this.unitatMesuraService = unitatMesuraService;
        this.proveidorService = proveidorService;
        this.materiaPrimeraService = materiaPrimeraService;
    }


    // GUARDAR UNITAT DE MESURA DES DEL FORMULARI D'ALBARÀ DE PROVEÏDOR
    @PostMapping("/save-from-albara-proveidor")
    public String guardarUnitatMesuraDesAlbaraProveidor(
            @ModelAttribute("albaraProveidor") AlbaraProveidor albaraProveidor,
            @RequestParam(value = "novaUnitatMesura", required = false) String novaUnitatMesura,
            @RequestParam(value = "indexUnitatMesura", required = false) Integer indexUnitatMesura,
            Model model) {

        // Assegura que el formulari sempre tingui almenys un lot
        if (albaraProveidor.getLots() == null || albaraProveidor.getLots().isEmpty()) {
            List<LotProveidor> lots = new ArrayList<>();
            lots.add(new LotProveidor());
            albaraProveidor.setLots(lots);
        }

        try {
            UnitatMesura unitatMesura = unitatMesuraService.createUnitatMesura(novaUnitatMesura);

            if (indexUnitatMesura != null
                    && indexUnitatMesura >= 0
                    && indexUnitatMesura < albaraProveidor.getLots().size()) {

                albaraProveidor.getLots().get(indexUnitatMesura).setUnitats(unitatMesura.getNom());
            }

            model.addAttribute("infoUnitatMesura", "Unitat de mesura creada correctament.");
        }
        catch (RuntimeException e) {
            model.addAttribute("errorUnitatMesura", e.getMessage());
            model.addAttribute("novaUnitatMesuraValor", novaUnitatMesura);
        }

        model.addAttribute("unitatMesuraPanelObert", true);
        model.addAttribute("unitatMesuraPanelIndex", indexUnitatMesura);

        model.addAttribute("albaraProveidor", albaraProveidor);
        model.addAttribute("proveidors", proveidorService.getAllProveidors());
        model.addAttribute("materiesPrimeres", materiaPrimeraService.getAllMateriesPrimeres());
        model.addAttribute("unitatsMesura", unitatMesuraService.getAllUnitatsMesura());

        return "albaransProveidor/formAlbaraProveidor";
    }
}
