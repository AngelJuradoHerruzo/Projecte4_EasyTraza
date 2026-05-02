package cat.copernic.easytraza.controller;

import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import cat.copernic.easytraza.entities.AlbaraProveidor;
import cat.copernic.easytraza.entities.LotProveidor;
import cat.copernic.easytraza.service.AlbaraProveidorService;
import cat.copernic.easytraza.service.MateriaPrimeraService;
import cat.copernic.easytraza.service.ProveidorService;
import cat.copernic.easytraza.service.UsuariService;

@Controller
@RequestMapping("/albarans-proveidor")
public class AlbaraProveidorWebController {

    // ---------------------------- SERVICE I CONSTRUCTOR ----------------------------
    private final AlbaraProveidorService albaraProveidorService;
    private final ProveidorService proveidorService;
    private final UsuariService usuariService;
    private final MateriaPrimeraService materiaPrimeraService;

    public AlbaraProveidorWebController(AlbaraProveidorService albaraProveidorService,
                                        ProveidorService proveidorService,
                                        UsuariService usuariService,
                                        MateriaPrimeraService materiaPrimeraService) {
        this.albaraProveidorService = albaraProveidorService;
        this.proveidorService = proveidorService;
        this.usuariService = usuariService;
        this.materiaPrimeraService = materiaPrimeraService;
    }

    // LLISTAR ALBARANS DE PROVEÏDOR
    @GetMapping("/list")
    public String llistarAlbaransProveidor(Model model) {
        model.addAttribute("albarans", albaraProveidorService.getAllAlbaransProveidor());
        return "albaransProveidor/llistarAlbaransProveidor";
    }


    // FORMULARI CREAR ALBARÀ DE PROVEÏDOR
    @GetMapping("/new")
    public String formCrearAlbaraProveidor(Model model) {
        AlbaraProveidor albaraProveidor = new AlbaraProveidor();

        List<LotProveidor> lots = new ArrayList<>();
        lots.add(new LotProveidor());

        albaraProveidor.setLots(lots);

        model.addAttribute("albaraProveidor", albaraProveidor);
        model.addAttribute("proveidors", proveidorService.getAllProveidors());
        model.addAttribute("usuaris", usuariService.getAllUsuaris());
        model.addAttribute("materiesPrimeres", materiaPrimeraService.getAllMateriesPrimeres());

        return "albaransProveidor/formAlbaraProveidor";
    }


    // GUARDAR ALBARÀ DE PROVEÏDOR
    @PostMapping("/save")
    public String guardarAlbaraProveidor(@ModelAttribute("albaraProveidor") AlbaraProveidor albaraProveidor, Model model) {
        try {
            albaraProveidorService.createAlbaraProveidor(albaraProveidor);
            return "redirect:/albarans-proveidor/list";
        }
        catch (RuntimeException e) {
            model.addAttribute("error", e.getMessage());
            model.addAttribute("albaraProveidor", albaraProveidor);
            return "albaransProveidor/formAlbaraProveidor";
        }
    }


    // FORMULARI EDITAR ALBARÀ DE PROVEÏDOR
    @GetMapping("/edit/{id}")
    public String formEditarAlbaraProveidor(@PathVariable Long id, Model model) {
        AlbaraProveidor albaraProveidor = albaraProveidorService.getAlbaraProveidorById(id);

        if (albaraProveidor == null) {
            return "redirect:/albarans-proveidor/list";
        }

        model.addAttribute("albaraProveidor", albaraProveidor);
        return "albaransProveidor/formAlbaraProveidor";
    }


    // ACTUALITZAR ALBARÀ DE PROVEÏDOR
    @PostMapping("/update/{id}")
    public String updateAlbaraProveidor(@PathVariable Long id,
                                        @ModelAttribute("albaraProveidor") AlbaraProveidor albaraProveidor,
                                        Model model) {
        try {
            albaraProveidorService.updateAlbaraProveidor(id, albaraProveidor);
            return "redirect:/albarans-proveidor/list";
        }
        catch (RuntimeException e) {
            model.addAttribute("error", e.getMessage());
            model.addAttribute("albaraProveidor", albaraProveidor);
            return "albaransProveidor/formAlbaraProveidor";
        }
    }


    // ELIMINAR ALBARÀ DE PROVEÏDOR
    @GetMapping("/delete/{id}")
    public String deleteAlbaraProveidor(@PathVariable Long id) {
        albaraProveidorService.deleteAlbaraProveidor(id);
        return "redirect:/albarans-proveidor/list";
    }

    //OCR
    @GetMapping("/ocr")
    public String ocrView() {
        return "albaransProveidor/ocrAlbaraProveidor";
    }
}
