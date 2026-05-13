package cat.copernic.easytraza.controller;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import cat.copernic.easytraza.entities.AlbaraProveidor;
import cat.copernic.easytraza.entities.LotProveidor;
import cat.copernic.easytraza.service.AlbaraProveidorService;
import cat.copernic.easytraza.service.MateriaPrimeraService;
import cat.copernic.easytraza.service.OcrAlbaraProveidorService;
import cat.copernic.easytraza.service.ProveidorService;
import cat.copernic.easytraza.service.UnitatMesuraService;
import cat.copernic.easytraza.service.UsuariService;

@Controller
@RequestMapping("/albarans-proveidor")
public class AlbaraProveidorWebController {

    // ---------------------------- SERVICES I CONSTRUCTOR ----------------------------
    private final AlbaraProveidorService albaraProveidorService;
    private final ProveidorService proveidorService;
    private final UsuariService usuariService;
    private final MateriaPrimeraService materiaPrimeraService;
    private final OcrAlbaraProveidorService ocrAlbaraProveidorService;
    private final UnitatMesuraService unitatMesuraService;

    public AlbaraProveidorWebController(AlbaraProveidorService albaraProveidorService,
                                        ProveidorService proveidorService,
                                        UsuariService usuariService,
                                        MateriaPrimeraService materiaPrimeraService,
                                        OcrAlbaraProveidorService ocrAlbaraProveidorService,
                                        UnitatMesuraService unitatMesuraService) {
        this.albaraProveidorService = albaraProveidorService;
        this.proveidorService = proveidorService;
        this.usuariService = usuariService;
        this.materiaPrimeraService = materiaPrimeraService;
        this.ocrAlbaraProveidorService = ocrAlbaraProveidorService;
        this.unitatMesuraService = unitatMesuraService;
    }


    // LLISTAR ALBARANS DE PROVEÏDOR
    @GetMapping("/list")
    public String llistarAlbaransProveidor(Model model) {
        model.addAttribute("albaransPerProveidor", albaraProveidorService.getAlbaransAgrupatsPerProveidor());
        model.addAttribute("albaraProveidorService", albaraProveidorService);

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

        List<LotProveidor> lots = new ArrayList<>();
        lots.add(new LotProveidor());

        albaraProveidor.setLots(lots);

        model.addAttribute("albaraProveidor", albaraProveidor);
        model.addAttribute("proveidors", proveidorService.getAllProveidors());
        model.addAttribute("usuaris", usuariService.getAllUsuaris());
        model.addAttribute("materiesPrimeres", materiaPrimeraService.getAllMateriesPrimeres());
        model.addAttribute("unitatsMesura", unitatMesuraService.getAllUnitatsMesura());

        return "albaransProveidor/formAlbaraProveidor";
    }


    // GUARDAR ALBARÀ DE PROVEÏDOR
    @PostMapping("/save")
    public String guardarAlbaraProveidor(@ModelAttribute("albaraProveidor") AlbaraProveidor albaraProveidor,
                                         @RequestParam(value = "ocrImageBase64", required = false) String ocrImageBase64,
                                         @RequestParam(value = "ocrImageOriginalName", required = false) String ocrImageOriginalName,
                                         @RequestParam(value = "imatgeAlbara", required = false) MultipartFile imatgeAlbara,
                                         Model model) {
        try {
            albaraProveidorService.createAlbaraProveidor(albaraProveidor, ocrImageBase64, ocrImageOriginalName, imatgeAlbara);
            return "redirect:/albarans-proveidor/list";
        }
        catch (RuntimeException e) {
            model.addAttribute("error", e.getMessage());
            model.addAttribute("albaraProveidor", albaraProveidor);
            model.addAttribute("ocrImageBase64", ocrImageBase64);
            model.addAttribute("ocrImageOriginalName", ocrImageOriginalName);
            model.addAttribute("proveidors", proveidorService.getAllProveidors());
            model.addAttribute("usuaris", usuariService.getAllUsuaris());
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
            if (!albaraProveidorService.esModificable(albaraProveidor)) {
                throw new RuntimeException("No es pot modificar aquest albarà perquè té algun lot que no està en estoc.");
            }

            model.addAttribute("albaraProveidor", albaraProveidor);
            model.addAttribute("proveidors", proveidorService.getAllProveidors());
            model.addAttribute("usuaris", usuariService.getAllUsuaris());
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
                                        @RequestParam(value = "ocrImageBase64", required = false) String ocrImageBase64,
                                        @RequestParam(value = "ocrImageOriginalName", required = false) String ocrImageOriginalName,
                                        @RequestParam(value = "imatgeAlbara", required = false) MultipartFile imatgeAlbara,
                                        Model model) {
        try {
            albaraProveidorService.updateAlbaraProveidor(id, albaraProveidor, ocrImageBase64, ocrImageOriginalName, imatgeAlbara);
            return "redirect:/albarans-proveidor/list";
        }
        catch (RuntimeException e) {
            albaraProveidor.setId(id);

            model.addAttribute("error", e.getMessage());
            model.addAttribute("albaraProveidor", albaraProveidor);
            model.addAttribute("ocrImageBase64", ocrImageBase64);
            model.addAttribute("ocrImageOriginalName", ocrImageOriginalName);
            model.addAttribute("proveidors", proveidorService.getAllProveidors());
            model.addAttribute("usuaris", usuariService.getAllUsuaris());
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


    // PROCESSAR OCR SENSE GUARDAR LA IMATGE DEFINITIVAMENT
    @PostMapping("/ocr/process")
    public String processOcr(@RequestParam("file") MultipartFile file, Model model) {
        try {
            AlbaraProveidor albaraProveidor = ocrAlbaraProveidorService.processarImatge(file);

            String ocrImageBase64 = ocrAlbaraProveidorService.convertirImatgeBase64(file);
            String ocrImageOriginalName = file.getOriginalFilename();

            if (albaraProveidor.getLots() == null || albaraProveidor.getLots().isEmpty()) {
                List<LotProveidor> lots = new ArrayList<>();
                lots.add(new LotProveidor());
                albaraProveidor.setLots(lots);
            }

            model.addAttribute("albaraProveidor", albaraProveidor);
            model.addAttribute("ocrImageBase64", ocrImageBase64);
            model.addAttribute("ocrImageOriginalName", ocrImageOriginalName);
            model.addAttribute("ocrInfo", "S'ha processat la imatge de l'albarà. Revisa les dades abans de guardar.");
            model.addAttribute("proveidors", proveidorService.getAllProveidors());
            model.addAttribute("usuaris", usuariService.getAllUsuaris());
            model.addAttribute("materiesPrimeres", materiaPrimeraService.getAllMateriesPrimeres());
            model.addAttribute("unitatsMesura", unitatMesuraService.getAllUnitatsMesura());

            return "albaransProveidor/formAlbaraProveidor";
        }
        catch (RuntimeException e) {
            AlbaraProveidor albaraProveidor = new AlbaraProveidor();

            List<LotProveidor> lots = new ArrayList<>();
            lots.add(new LotProveidor());

            albaraProveidor.setLots(lots);

            model.addAttribute("error", e.getMessage());
            model.addAttribute("albaraProveidor", albaraProveidor);
            model.addAttribute("proveidors", proveidorService.getAllProveidors());
            model.addAttribute("usuaris", usuariService.getAllUsuaris());
            model.addAttribute("materiesPrimeres", materiaPrimeraService.getAllMateriesPrimeres());
            model.addAttribute("unitatsMesura", unitatMesuraService.getAllUnitatsMesura());

            return "albaransProveidor/formAlbaraProveidor";
        }
    }
}