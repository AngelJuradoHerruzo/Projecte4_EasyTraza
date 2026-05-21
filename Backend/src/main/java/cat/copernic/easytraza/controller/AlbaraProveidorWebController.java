package cat.copernic.easytraza.controller;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
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

import cat.copernic.easytraza.dto.OcrAlbaraPendent;
import cat.copernic.easytraza.dto.OcrLiniaDto;
import cat.copernic.easytraza.dto.OcrResultatAlbaraProveidorDto;
import cat.copernic.easytraza.entities.AlbaraProveidor;
import cat.copernic.easytraza.entities.LotProveidor;
import cat.copernic.easytraza.entities.MateriaPrimera;
import cat.copernic.easytraza.entities.Proveidor;
import cat.copernic.easytraza.service.AlbaraProveidorService;
import cat.copernic.easytraza.service.MateriaPrimeraService;
import cat.copernic.easytraza.service.OcrAlbaraProveidorService;
import cat.copernic.easytraza.service.ProveidorService;
import cat.copernic.easytraza.service.UnitatMesuraService;
import cat.copernic.easytraza.service.ocr.OcrUtils;
import jakarta.servlet.http.HttpSession;

@Controller
@RequestMapping("/albarans-proveidor")
public class AlbaraProveidorWebController {

    // ---------------------------- SERVICES I CONSTRUCTOR ----------------------------
    private final AlbaraProveidorService albaraProveidorService;
    private final ProveidorService proveidorService;
    private final MateriaPrimeraService materiaPrimeraService;
    private final UnitatMesuraService unitatMesuraService;
    private final OcrAlbaraProveidorService ocrAlbaraProveidorService;

    public AlbaraProveidorWebController(AlbaraProveidorService albaraProveidorService,
                                        ProveidorService proveidorService,
                                        MateriaPrimeraService materiaPrimeraService,
                                        UnitatMesuraService unitatMesuraService,
                                        OcrAlbaraProveidorService ocrAlbaraProveidorService) {
        this.albaraProveidorService = albaraProveidorService;
        this.proveidorService = proveidorService;
        this.materiaPrimeraService = materiaPrimeraService;
        this.unitatMesuraService = unitatMesuraService;
        this.ocrAlbaraProveidorService = ocrAlbaraProveidorService;
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
        carregarDadesFormulari(model);

        return "albaransProveidor/formAlbaraProveidor";
    }


    // ESCANEJAR OCR I RETORNAR EL MATEIX FORMULARI PREOMPLERT
    @PostMapping("/ocr")
    public String escanejarOcr(@RequestParam(value = "documentOcr", required = false) MultipartFile documentOcr,
                               Model model) {
        try {
            OcrResultatAlbaraProveidorDto resultatOcr = ocrAlbaraProveidorService.processarDocument(documentOcr);
            completarAssociacionsOcr(resultatOcr);

            AlbaraProveidor albaraProveidor = convertirResultatOcrAFormulari(resultatOcr);

            model.addAttribute("albaraProveidor", albaraProveidor);
            model.addAttribute("ocrResultat", resultatOcr);
            model.addAttribute("ocrDocumentTemporalId", resultatOcr.getOcrDocumentTemporalId());
            model.addAttribute("ocrDocumentNomOriginal", resultatOcr.getOcrDocumentNomOriginal());
            model.addAttribute("ocrDocumentUrlTemporal", resultatOcr.getOcrDocumentUrlTemporal());
            model.addAttribute("ocrDocumentContentType", resultatOcr.getOcrDocumentContentType());
            carregarDadesFormulari(model);

            return "albaransProveidor/formAlbaraProveidor";
        }
        catch (RuntimeException e) {
            AlbaraProveidor albaraProveidor = new AlbaraProveidor();
            albaraProveidor.setDataRecepcio(LocalDate.now());
            albaraProveidor.setLots(new ArrayList<>(List.of(new LotProveidor())));

            model.addAttribute("error", e.getMessage());
            model.addAttribute("albaraProveidor", albaraProveidor);
            carregarDadesFormulari(model);

            return "albaransProveidor/formAlbaraProveidor";
        }
    }


    // RECARREGAR LLISTES DEL FORMULARI SENSE PERDRE LES DADES ESCRITES
    @PostMapping("/reload-form")
    public String recarregarFormulari(@ModelAttribute("albaraProveidor") AlbaraProveidor albaraProveidor,
                                      @RequestParam(value = "ocrDocumentTemporalId", required = false) String ocrDocumentTemporalId,
                                      Model model) {
        assegurarLotsFormulari(albaraProveidor);

        model.addAttribute("albaraProveidor", albaraProveidor);
        model.addAttribute("info", "Llistes actualitzades. Ja pots seleccionar els elements creats.");
        model.addAttribute("ocrDocumentTemporalId", ocrDocumentTemporalId);
        afegirDadesDocumentTemporal(model, ocrDocumentTemporalId);
        carregarDadesFormulari(model);

        return "albaransProveidor/formAlbaraProveidor";
    }


    // GUARDAR ALBARÀ DE PROVEÏDOR
    @PostMapping("/save")
    public String guardarAlbaraProveidor(@ModelAttribute("albaraProveidor") AlbaraProveidor albaraProveidor,
                                         @RequestParam(value = "imatgeAlbara", required = false) MultipartFile imatgeAlbara,
                                         @RequestParam(value = "ocrDocumentTemporalId", required = false) String ocrDocumentTemporalId,
                                         HttpSession session,
                                         Model model) {
        try {
            albaraProveidorService.createAlbaraProveidor(albaraProveidor, imatgeAlbara, ocrDocumentTemporalId, session);
            return "redirect:/albarans-proveidor/list";
        }
        catch (RuntimeException e) {
            assegurarLotsFormulari(albaraProveidor);

            model.addAttribute("error", e.getMessage());
            model.addAttribute("albaraProveidor", albaraProveidor);
            model.addAttribute("ocrDocumentTemporalId", ocrDocumentTemporalId);
            afegirDadesDocumentTemporal(model, ocrDocumentTemporalId);
            carregarDadesFormulari(model);

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
            assegurarLotsFormulari(albaraProveidor);

            model.addAttribute("albaraProveidor", albaraProveidor);
            carregarDadesFormulari(model);

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
                                        @RequestParam(value = "ocrDocumentTemporalId", required = false) String ocrDocumentTemporalId,
                                        HttpSession session,
                                        Model model) {
        try {
            albaraProveidorService.updateAlbaraProveidor(id, albaraProveidor, imatgeAlbara, ocrDocumentTemporalId, session);
            return "redirect:/albarans-proveidor/list";
        }
        catch (RuntimeException e) {
            albaraProveidor.setId(id);
            assegurarLotsFormulari(albaraProveidor);

            model.addAttribute("error", e.getMessage());
            model.addAttribute("albaraProveidor", albaraProveidor);
            model.addAttribute("ocrDocumentTemporalId", ocrDocumentTemporalId);
            afegirDadesDocumentTemporal(model, ocrDocumentTemporalId);
            carregarDadesFormulari(model);

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


    // CARREGAR DADES COMUNES DEL FORMULARI
    private void carregarDadesFormulari(Model model) {
        model.addAttribute("proveidors", proveidorService.getAllProveidors());
        model.addAttribute("materiesPrimeres", materiaPrimeraService.getAllMateriesPrimeres());
        model.addAttribute("unitatsMesura", unitatMesuraService.getAllUnitatsMesura());
    }


    // AFEGIR DADES DE PREVISUALITZACIÓ DEL DOCUMENT OCR TEMPORAL
    private void afegirDadesDocumentTemporal(Model model, String ocrDocumentTemporalId) {
        if (ocrDocumentTemporalId == null || ocrDocumentTemporalId.isBlank()) {
            return;
        }

        try {
            model.addAttribute("ocrDocumentNomOriginal", ocrDocumentTemporalId);
            model.addAttribute("ocrDocumentUrlTemporal", ocrAlbaraProveidorService.obtenirUrlDocumentTemporal(ocrDocumentTemporalId));
            model.addAttribute("ocrDocumentContentType", ocrAlbaraProveidorService.obtenirContentTypeDocumentTemporal(ocrDocumentTemporalId));
        }
        catch (RuntimeException ignored) {
            model.addAttribute("ocrDocumentNomOriginal", ocrDocumentTemporalId);
        }
    }


    // ASSEGURAR QUE EL FORMULARI TINGUI COM A MÍNIM UNA LÍNIA
    private void assegurarLotsFormulari(AlbaraProveidor albaraProveidor) {
        if (albaraProveidor.getLots() == null || albaraProveidor.getLots().isEmpty()) {
            albaraProveidor.setLots(new ArrayList<>(List.of(new LotProveidor())));
        }
    }


    // CONVERTIR EL RESULTAT OCR A L'ENTITAT USADA PEL FORMULARI
    private AlbaraProveidor convertirResultatOcrAFormulari(OcrResultatAlbaraProveidorDto resultatOcr) {
        AlbaraProveidor albaraProveidor = new AlbaraProveidor();
        OcrAlbaraPendent pendent = resultatOcr.getAlbaraPendent();

        albaraProveidor.setNumeroAlbara(pendent.getNumeroAlbara());
        albaraProveidor.setDataRecepcio(convertirDataOcr(pendent.getDataAlbara()));

        if (pendent.getProveidorId() != null) {
            albaraProveidor.setProveidor(proveidorService.getProveidorById(pendent.getProveidorId()));
        }

        List<LotProveidor> lots = new ArrayList<>();

        if (pendent.getLinies() != null) {
            for (OcrLiniaDto liniaOcr : pendent.getLinies()) {
                lots.add(convertirLiniaOcrAFormulari(liniaOcr));
            }
        }

        if (lots.isEmpty()) {
            lots.add(new LotProveidor());
        }

        albaraProveidor.setLots(lots);

        return albaraProveidor;
    }


    // CONVERTIR UNA LÍNIA OCR A LOT TEMPORAL DE FORMULARI
    private LotProveidor convertirLiniaOcrAFormulari(OcrLiniaDto liniaOcr) {
        LotProveidor lot = new LotProveidor();
        lot.setIdentificadorLot(liniaOcr.getIdentificadorLot());
        lot.setQuantitat(convertirQuantitatOcr(liniaOcr.getQuantitat()));
        lot.setUnitats(liniaOcr.getUnitat());

        if (liniaOcr.getMateriaPrimeraId() != null) {
            lot.setMateriaPrimera(materiaPrimeraService.getMateriaPrimeraById(liniaOcr.getMateriaPrimeraId()));
        }

        return lot;
    }


    // COMPLETAR ASSOCIACIONS AMB ELEMENTS EXISTENTS DE BASE DE DADES
    private void completarAssociacionsOcr(OcrResultatAlbaraProveidorDto resultatOcr) {
        if (resultatOcr == null || resultatOcr.getAlbaraPendent() == null) {
            return;
        }

        OcrAlbaraPendent pendent = resultatOcr.getAlbaraPendent();
        associarProveidorOcr(pendent);
        associarMateriesPrimeresOcr(pendent);
    }


    // ASSOCIAR PROVEÏDOR OCR SI EXISTEIX A LA BASE DE DADES
    private void associarProveidorOcr(OcrAlbaraPendent pendent) {
        Proveidor proveidor = buscarProveidor(pendent.getProveidorCifDetectat(), pendent.getProveidorDetectat());

        if (proveidor != null) {
            pendent.setProveidorId(proveidor.getId());
            pendent.setProveidorNomAssociat(proveidor.getNomProveidor());
            pendent.setProveidorTrobat(true);
            return;
        }

        pendent.setProveidorTrobat(false);

        if (pendent.getProveidorDetectat() != null && !pendent.getProveidorDetectat().isBlank()) {
            pendent.afegirAvis("Proveïdor detectat per OCR no trobat a la base de dades: " + pendent.getProveidorDetectat());
        }
    }


    // ASSOCIAR MATÈRIES PRIMERES OCR SI EXISTEIXEN A LA BASE DE DADES
    private void associarMateriesPrimeresOcr(OcrAlbaraPendent pendent) {
        if (pendent.getLinies() == null) {
            return;
        }

        for (OcrLiniaDto linia : pendent.getLinies()) {
            MateriaPrimera materiaPrimera = buscarMateriaPrimera(linia.getMateriaPrimeraDetectada());

            if (materiaPrimera != null) {
                linia.setMateriaPrimeraId(materiaPrimera.getId());
                linia.setMateriaPrimeraNomAssociada(materiaPrimera.getNomMateria());
                linia.setMateriaPrimeraTrobada(true);
                continue;
            }

            linia.setMateriaPrimeraTrobada(false);

            if (linia.getMateriaPrimeraDetectada() != null && !linia.getMateriaPrimeraDetectada().isBlank()) {
                linia.afegirAvis("Matèria primera no trobada. OCR: " + linia.getMateriaPrimeraDetectada());
            }
        }
    }


    // BUSCAR PROVEÏDOR PER CIF O NOM FLEXIBLE
    private Proveidor buscarProveidor(String cifDetectat, String nomDetectat) {
        for (Proveidor proveidor : proveidorService.getAllProveidors()) {
            if (cifDetectat != null && !cifDetectat.isBlank()
                    && proveidor.getCif() != null
                    && proveidor.getCif().replace(" ", "").equalsIgnoreCase(cifDetectat.replace(" ", ""))) {
                return proveidor;
            }
        }

        if (nomDetectat == null || nomDetectat.isBlank()) {
            return null;
        }

        String nomOcr = OcrUtils.normalitzarPerComparar(nomDetectat);

        for (Proveidor proveidor : proveidorService.getAllProveidors()) {
            String nomBd = OcrUtils.normalitzarPerComparar(proveidor.getNomProveidor());

            if (nomBd.equals(nomOcr) || nomBd.contains(nomOcr) || nomOcr.contains(nomBd)) {
                return proveidor;
            }
        }

        return null;
    }


    // BUSCAR MATÈRIA PRIMERA PER NOM FLEXIBLE
    private MateriaPrimera buscarMateriaPrimera(String materiaDetectada) {
        if (materiaDetectada == null || materiaDetectada.isBlank()) {
            return null;
        }

        String materiaOcr = OcrUtils.normalitzarPerComparar(materiaDetectada);

        for (MateriaPrimera materiaPrimera : materiaPrimeraService.getAllMateriesPrimeres()) {
            String materiaBd = OcrUtils.normalitzarPerComparar(materiaPrimera.getNomMateria());

            if (materiaBd.equals(materiaOcr) || materiaBd.contains(materiaOcr) || materiaOcr.contains(materiaBd)) {
                return materiaPrimera;
            }
        }

        return null;
    }


    // CONVERTIR DATA OCR A LOCALDATE
    private LocalDate convertirDataOcr(String dataOcr) {
        if (dataOcr == null || dataOcr.isBlank()) {
            return LocalDate.now();
        }

        try {
            return LocalDate.parse(dataOcr);
        }
        catch (DateTimeParseException ignored) {
            // Continua amb altres formats habituals.
        }

        for (String patro : List.of("dd/MM/yyyy", "dd-MM-yyyy", "d/M/yyyy", "d-M-yyyy")) {
            try {
                return LocalDate.parse(dataOcr, DateTimeFormatter.ofPattern(patro));
            }
            catch (DateTimeParseException ignored) {
                // Es prova el següent format.
            }
        }

        return LocalDate.now();
    }


    // CONVERTIR QUANTITAT OCR A ENTER PER AL FORMULARI ACTUAL
    private Integer convertirQuantitatOcr(Double quantitat) {
        if (quantitat == null) {
            return null;
        }

        return (int) Math.round(quantitat);
    }
}
