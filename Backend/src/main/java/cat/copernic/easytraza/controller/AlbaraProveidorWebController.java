package cat.copernic.easytraza.controller;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import cat.copernic.easytraza.entities.AlbaraProveidor;
import cat.copernic.easytraza.entities.LotProveidor;
import cat.copernic.easytraza.service.AlbaraProveidorService;
import cat.copernic.easytraza.service.MateriaPrimeraService;
import cat.copernic.easytraza.service.ProveidorService;
import cat.copernic.easytraza.service.UnitatMesuraService;
import jakarta.servlet.http.HttpSession;

@Controller
@RequestMapping("/albarans-proveidor")
public class AlbaraProveidorWebController {

    // ---------------------------- SERVICES I CONSTRUCTOR ----------------------------
    private final AlbaraProveidorService albaraProveidorService;
    private final ProveidorService proveidorService;
    private final MateriaPrimeraService materiaPrimeraService;
    private final UnitatMesuraService unitatMesuraService;

    public AlbaraProveidorWebController(AlbaraProveidorService albaraProveidorService,
                                        ProveidorService proveidorService,
                                        MateriaPrimeraService materiaPrimeraService,
                                        UnitatMesuraService unitatMesuraService) {
        this.albaraProveidorService = albaraProveidorService;
        this.proveidorService = proveidorService;
        this.materiaPrimeraService = materiaPrimeraService;
        this.unitatMesuraService = unitatMesuraService;
    }


    // LLISTAR ALBARANS DE PROVEÏDOR
    @GetMapping("/list")
    public String llistarAlbaransProveidor(@RequestParam(value = "proveidor", required = false) String proveidor,
                                           @RequestParam(value = "numeroAlbara", required = false) String numeroAlbara,
                                           @RequestParam(value = "identificadorLot", required = false) String identificadorLot,
                                           @RequestParam(value = "dataRecepcio", required = false) String dataRecepcio,
                                           @RequestParam(value = "receptor", required = false) String receptor,
                                           Model model) {

        model.addAttribute("albaransPerProveidor", albaraProveidorService.getAlbaransAgrupatsPerProveidor(
                proveidor,
                numeroAlbara,
                identificadorLot,
                dataRecepcio,
                receptor
        ));
        model.addAttribute("albaraProveidorService", albaraProveidorService);
        model.addAttribute("proveidor", proveidor);
        model.addAttribute("numeroAlbara", numeroAlbara);
        model.addAttribute("identificadorLot", identificadorLot);
        model.addAttribute("dataRecepcio", dataRecepcio);
        model.addAttribute("receptor", receptor);

        return "albaransProveidor/llistarAlbaransProveidor";
    }


    // DETALL ALBARÀ DE PROVEÏDOR
    @GetMapping("/detail/{id}")
    public String detallAlbaraProveidor(@PathVariable Long id, Model model) {
        AlbaraProveidor albaraProveidor = albaraProveidorService.getAlbaraProveidorDetallById(id);

        if (albaraProveidor == null) {
            return "redirect:/albarans-proveidor/list";
        }

        model.addAttribute("albaraProveidor", albaraProveidor);

        return "albaransProveidor/detallAlbaraProveidor";
    }


    // FORMULARI CREAR ALBARÀ DE PROVEÏDOR
    @GetMapping("/new")
    public String formCrearAlbaraProveidor(Model model) {
        AlbaraProveidor albaraProveidor = new AlbaraProveidor();
        albaraProveidor.setDataRecepcio(LocalDate.now());
        albaraProveidor.setLots(new ArrayList<>(List.of(new LotProveidor())));

        model.addAttribute("albaraProveidor", albaraProveidor);
        model.addAttribute("proveidors", proveidorService.getAllProveidors());
        model.addAttribute("materiesPrimeres", materiaPrimeraService.getAllMateriesPrimeres());
        model.addAttribute("unitatsMesura", unitatMesuraService.getAllUnitatsMesura());

        return "albaransProveidor/formAlbaraProveidor";
    }


    // GUARDAR ALBARÀ DE PROVEÏDOR
    @PostMapping("/save")
    public String guardarAlbaraProveidor(@ModelAttribute("albaraProveidor") AlbaraProveidor albaraProveidor,
                                         @RequestParam(value = "imatgeAlbara", required = false) MultipartFile imatgeAlbara,
                                         HttpSession session,
                                         Model model) {
        try {
            albaraProveidorService.createAlbaraProveidor(albaraProveidor, imatgeAlbara, session);
            return "redirect:/albarans-proveidor/list";
        }
        catch (RuntimeException e) {
            if (albaraProveidor.getLots() == null || albaraProveidor.getLots().isEmpty()) {
                albaraProveidor.setLots(new ArrayList<>(List.of(new LotProveidor())));
            }

            model.addAttribute("error", e.getMessage());
            model.addAttribute("albaraProveidor", albaraProveidor);
            model.addAttribute("proveidors", proveidorService.getAllProveidors());
            model.addAttribute("materiesPrimeres", materiaPrimeraService.getAllMateriesPrimeres());
            model.addAttribute("unitatsMesura", unitatMesuraService.getAllUnitatsMesura());

            return "albaransProveidor/formAlbaraProveidor";
        }
    }


    // FORMULARI EDITAR ALBARÀ DE PROVEÏDOR
    @GetMapping("/edit/{id}")
    public String formEditarAlbaraProveidor(@PathVariable Long id, Model model) {
        AlbaraProveidor albaraProveidor = albaraProveidorService.getAlbaraProveidorDetallById(id);

        if (albaraProveidor == null) {
            return "redirect:/albarans-proveidor/list";
        }

        try {
            albaraProveidorService.validarAlbaraModificable(albaraProveidor);

            if (albaraProveidor.getLots() == null || albaraProveidor.getLots().isEmpty()) {
                albaraProveidor.setLots(new ArrayList<>(List.of(new LotProveidor())));
            }

            model.addAttribute("albaraProveidor", albaraProveidor);
            model.addAttribute("proveidors", proveidorService.getAllProveidors());
            model.addAttribute("materiesPrimeres", materiaPrimeraService.getAllMateriesPrimeres());
            model.addAttribute("unitatsMesura", unitatMesuraService.getAllUnitatsMesura());

            return "albaransProveidor/formAlbaraProveidor";
        }
        catch (RuntimeException e) {
            model.addAttribute("error", e.getMessage());
            model.addAttribute("albaransPerProveidor", albaraProveidorService.getAlbaransAgrupatsPerProveidor());
            model.addAttribute("albaraProveidorService", albaraProveidorService);

            return "albaransProveidor/llistarAlbaransProveidor";
        }
    }


    // ACTUALITZAR ALBARÀ DE PROVEÏDOR
    @PostMapping("/update/{id}")
    public String updateAlbaraProveidor(@PathVariable Long id,
                                        @ModelAttribute("albaraProveidor") AlbaraProveidor albaraProveidor,
                                        @RequestParam(value = "imatgeAlbara", required = false) MultipartFile imatgeAlbara,
                                        HttpSession session,
                                        Model model) {
        try {
            albaraProveidorService.updateAlbaraProveidor(id, albaraProveidor, imatgeAlbara, session);
            return "redirect:/albarans-proveidor/list";
        }
        catch (RuntimeException e) {
            albaraProveidor.setId(id);

            if (albaraProveidor.getLots() == null || albaraProveidor.getLots().isEmpty()) {
                albaraProveidor.setLots(new ArrayList<>(List.of(new LotProveidor())));
            }

            model.addAttribute("error", e.getMessage());
            model.addAttribute("albaraProveidor", albaraProveidor);
            model.addAttribute("proveidors", proveidorService.getAllProveidors());
            model.addAttribute("materiesPrimeres", materiaPrimeraService.getAllMateriesPrimeres());
            model.addAttribute("unitatsMesura", unitatMesuraService.getAllUnitatsMesura());

            return "albaransProveidor/formAlbaraProveidor";
        }
    }


    // ELIMINAR ALBARÀ DE PROVEÏDOR
    @GetMapping("/delete/{id}")
    public String deleteAlbaraProveidor(@PathVariable Long id, Model model) {
        try {
            albaraProveidorService.deleteAlbaraProveidor(id);
        }
        catch (RuntimeException e) {
            model.addAttribute("error", e.getMessage());
            model.addAttribute("albaransPerProveidor", albaraProveidorService.getAlbaransAgrupatsPerProveidor());
            model.addAttribute("albaraProveidorService", albaraProveidorService);

            return "albaransProveidor/llistarAlbaransProveidor";
        }

        return "redirect:/albarans-proveidor/list";
    }
}