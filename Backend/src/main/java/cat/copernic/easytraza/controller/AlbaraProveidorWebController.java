package cat.copernic.easytraza.controller;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import cat.copernic.easytraza.dto.OcrResultatAlbaraProveidorDto;
import cat.copernic.easytraza.entities.AlbaraProveidor;
import cat.copernic.easytraza.entities.LotProveidor;
import cat.copernic.easytraza.service.AlbaraProveidorService;
import cat.copernic.easytraza.service.MateriaPrimeraService;
import cat.copernic.easytraza.service.OcrAlbaraProveidorService;
import cat.copernic.easytraza.service.ProveidorService;
import cat.copernic.easytraza.service.UnitatMesuraService;
import jakarta.servlet.http.HttpSession;

@Controller
@RequestMapping("/albarans-proveidor")
public class AlbaraProveidorWebController {

    private static final Logger LOGGER = LoggerFactory.getLogger(AlbaraProveidorWebController.class);

    private static final String SESSION_OCR_RESULTAT = "ocrResultatAlbaraProveidor";

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
    public String formCrearAlbaraProveidor(Model model, HttpSession session) {
        session.removeAttribute(SESSION_OCR_RESULTAT);

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
                               HttpSession session,
                               Model model) {
        try {
            OcrResultatAlbaraProveidorDto resultatOcr = ocrAlbaraProveidorService.processarDocument(documentOcr);
            LOGGER.info("Document OCR de l'albarà de proveïdor processat correctament.");
            albaraProveidorService.completarAssociacionsOcr(resultatOcr);
            session.setAttribute(SESSION_OCR_RESULTAT, resultatOcr);

            AlbaraProveidor albaraProveidor = albaraProveidorService.convertirResultatOcrAFormulari(resultatOcr);

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
            LOGGER.warn("No s'ha pogut processar el document OCR de l'albarà de proveïdor: {}", e.getMessage());
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
                                      HttpSession session,
                                      Model model) {
        assegurarLotsFormulari(albaraProveidor);

        OcrResultatAlbaraProveidorDto resultatOcr = obtenirResultatOcrSessio(session);

        if (resultatOcr != null) {
            albaraProveidorService.netejarAvisosAssociacioOcr(resultatOcr);
            albaraProveidorService.completarAssociacionsOcr(resultatOcr);
            albaraProveidorService.aplicarAssociacionsOcrAlFormulari(albaraProveidor, resultatOcr);
            model.addAttribute("ocrResultat", resultatOcr);
            ocrDocumentTemporalId = obtenirDocumentTemporalId(ocrDocumentTemporalId, resultatOcr);
        }

        model.addAttribute("albaraProveidor", albaraProveidor);
        model.addAttribute("info", "Llistes actualitzades. Els elements creats s'han associat si coincideixen amb les dades OCR.");
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
            LOGGER.info("Albarà de proveïdor creat correctament.");
            session.removeAttribute(SESSION_OCR_RESULTAT);
            return "redirect:/albarans-proveidor/list";
        }
        catch (RuntimeException e) {
            LOGGER.warn("No s'ha pogut crear l'albarà de proveïdor: {}", e.getMessage());
            assegurarLotsFormulari(albaraProveidor);

            model.addAttribute("error", e.getMessage());
            model.addAttribute("albaraProveidor", albaraProveidor);
            model.addAttribute("ocrResultat", obtenirResultatOcrSessio(session));
            model.addAttribute("ocrDocumentTemporalId", ocrDocumentTemporalId);
            afegirDadesDocumentTemporal(model, ocrDocumentTemporalId);
            carregarDadesFormulari(model);

            return "albaransProveidor/formAlbaraProveidor";
        }
    }


    // FORMULARI EDITAR ALBARÀ DE PROVEÏDOR
    @GetMapping("/edit/{id}")
    public String formEditarAlbaraProveidor(@PathVariable Long id, Model model, HttpSession session) {
        session.removeAttribute(SESSION_OCR_RESULTAT);

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
            LOGGER.warn("No s'ha pogut carregar l'albarà de proveïdor amb identificador {} per editar-lo: {}", id, e.getMessage());
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
            LOGGER.info("Albarà de proveïdor amb identificador {} actualitzat correctament.", id);
            session.removeAttribute(SESSION_OCR_RESULTAT);
            return "redirect:/albarans-proveidor/list";
        }
        catch (RuntimeException e) {
            LOGGER.warn("No s'ha pogut actualitzar l'albarà de proveïdor amb identificador {}: {}", id, e.getMessage());
            albaraProveidor.setId(id);
            assegurarLotsFormulari(albaraProveidor);

            model.addAttribute("error", e.getMessage());
            model.addAttribute("albaraProveidor", albaraProveidor);
            model.addAttribute("ocrResultat", obtenirResultatOcrSessio(session));
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
            LOGGER.info("Albarà de proveïdor amb identificador {} eliminat correctament.", id);
        }
        catch (RuntimeException e) {
            LOGGER.warn("No s'ha pogut eliminar l'albarà de proveïdor amb identificador {}: {}", id, e.getMessage());
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
            LOGGER.warn("No s'ha pogut recuperar la vista prèvia del document OCR temporal.");
            model.addAttribute("ocrDocumentNomOriginal", ocrDocumentTemporalId);
        }
    }


    // OBTENIR RESULTAT OCR TEMPORAL CONSERVAT A LA SESSIÓ
    private OcrResultatAlbaraProveidorDto obtenirResultatOcrSessio(HttpSession session) {
        Object resultat = session.getAttribute(SESSION_OCR_RESULTAT);
        return resultat instanceof OcrResultatAlbaraProveidorDto
                ? (OcrResultatAlbaraProveidorDto) resultat
                : null;
    }


    // RECUPERAR LA REFERÈNCIA LLEUGERA AL DOCUMENT OCR QUAN ES RECARREGA EL FORMULARI
    private String obtenirDocumentTemporalId(String ocrDocumentTemporalId, OcrResultatAlbaraProveidorDto resultatOcr) {
        if (ocrDocumentTemporalId != null && !ocrDocumentTemporalId.isBlank()) {
            return ocrDocumentTemporalId;
        }

        return resultatOcr != null ? resultatOcr.getOcrDocumentTemporalId() : null;
    }


    // ASSEGURAR QUE EL FORMULARI TINGUI COM A MÍNIM UNA LÍNIA
    private void assegurarLotsFormulari(AlbaraProveidor albaraProveidor) {
        if (albaraProveidor.getLots() == null || albaraProveidor.getLots().isEmpty()) {
            albaraProveidor.setLots(new ArrayList<>(List.of(new LotProveidor())));
        }
    }
}
