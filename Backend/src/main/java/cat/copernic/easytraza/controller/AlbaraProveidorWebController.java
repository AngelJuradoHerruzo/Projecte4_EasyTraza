package cat.copernic.easytraza.controller;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
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

/**
 * CONTROLADOR WEB D'ALBARANS DE PROVEÏDOR.
 *
 * Gestionades les pantalles i operacions dels albarans de proveïdor, incloent-hi
 * la càrrega temporal de documents reconeguts mitjançant OCR.
 *
 * @author Ángel Jurado Herruz
 */
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
    private final MessageSource messageSource;

    public AlbaraProveidorWebController(AlbaraProveidorService albaraProveidorService,
                                        ProveidorService proveidorService,
                                        MateriaPrimeraService materiaPrimeraService,
                                        UnitatMesuraService unitatMesuraService,
                                        OcrAlbaraProveidorService ocrAlbaraProveidorService,
                                        MessageSource messageSource) {
        this.albaraProveidorService = albaraProveidorService;
        this.proveidorService = proveidorService;
        this.materiaPrimeraService = materiaPrimeraService;
        this.unitatMesuraService = unitatMesuraService;
        this.ocrAlbaraProveidorService = ocrAlbaraProveidorService;
        this.messageSource = messageSource;
    }


    /**
     * LLISTAT D'ALBARANS DE PROVEÏDOR.
     *
     * Preparada la vista amb els albarans de proveïdor filtrats pels criteris
     * de cerca rebuts des del formulari.
     *
     * @param proveidor nom del proveïdor utilitzat com a filtre
     * @param numeroAlbara número d'albarà utilitzat com a filtre
     * @param identificadorLot identificador del lot utilitzat com a filtre
     * @param dataRecepcio data de recepció utilitzada com a filtre
     * @param receptor receptor utilitzat com a filtre
     * @param model model de dades de la vista
     * @return vista del llistat d'albarans de proveïdor
     */
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


    /**
     * DETALL D'UN ALBARÀ DE PROVEÏDOR.
     *
     * Carregades les dades d'un albarà de proveïdor per mostrar-ne el detall
     * o redirigida la petició quan no existeix.
     *
     * @param id identificador de l'albarà de proveïdor
     * @param model model de dades de la vista
     * @return vista de detall o redirecció al llistat
     */
    @GetMapping("/detail/{id}")
    public String detallAlbaraProveidor(@PathVariable Long id, Model model) {
        AlbaraProveidor albaraProveidor = albaraProveidorService.getAlbaraProveidorDetallById(id);

        if (albaraProveidor == null) {
            return "redirect:/albarans-proveidor/list";
        }

        model.addAttribute("albaraProveidor", albaraProveidor);

        return "albaransProveidor/detallAlbaraProveidor";
    }


    /**
     * FORMULARI DE CREACIÓ D'ALBARÀ DE PROVEÏDOR.
     *
     * Preparat un albarà nou amb la data actual i una línia inicial,
     * eliminant qualsevol resultat OCR temporal anterior de la sessió.
     *
     * @param model model de dades de la vista
     * @param session sessió web de l'usuari
     * @return vista del formulari d'albarans de proveïdor
     */
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


    /**
     * ESCANEIG OCR DE L'ALBARÀ.
     *
     * Processat el document adjuntat, completades les associacions reconegudes
     * i retornat el formulari amb les dades detectades per a la seva revisió.
     *
     * @param documentOcr document adjuntat per al reconeixement OCR
     * @param session sessió web de l'usuari
     * @param model model de dades de la vista
     * @return vista del formulari amb les dades OCR o amb l'error detectat
     */
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


    /**
     * RECÀRREGA DEL FORMULARI.
     *
     * Recarregades les dades auxiliars del formulari sense perdre els valors
     * introduïts ni la referència al document OCR temporal.
     *
     * @param albaraProveidor dades actuals de l'albarà introduïdes al formulari
     * @param ocrDocumentTemporalId identificador del document OCR temporal
     * @param session sessió web de l'usuari
     * @param model model de dades de la vista
     * @return vista del formulari d'albarans de proveïdor
     */
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
        model.addAttribute("info", missatge("albaraProveidor.info.llistesActualitzades"));
        model.addAttribute("ocrDocumentTemporalId", ocrDocumentTemporalId);
        afegirDadesDocumentTemporal(model, ocrDocumentTemporalId);
        carregarDadesFormulari(model);

        return "albaransProveidor/formAlbaraProveidor";
    }


    /**
     * GUARDAT D'UN ALBARÀ DE PROVEÏDOR.
     *
     * Processat el formulari de creació, el document adjunt i les dades OCR
     * temporals per guardar l'albarà de proveïdor.
     *
     * @param albaraProveidor dades de l'albarà introduïdes al formulari
     * @param imatgeAlbara document adjunt de l'albarà
     * @param ocrDocumentTemporalId identificador del document OCR temporal
     * @param session sessió web de l'usuari
     * @param model model de dades de la vista
     * @return redirecció al llistat o vista del formulari amb errors
     */
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


    /**
     * FORMULARI D'EDICIÓ D'ALBARÀ DE PROVEÏDOR.
     *
     * Carregat l'albarà seleccionat per editar-lo i netejat qualsevol resultat
     * OCR temporal anterior de la sessió.
     *
     * @param id identificador de l'albarà de proveïdor
     * @param model model de dades de la vista
     * @param session sessió web de l'usuari
     * @return vista del formulari o redirecció al llistat
     */
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


    /**
     * ACTUALITZACIÓ D'UN ALBARÀ DE PROVEÏDOR.
     *
     * Processats els canvis de l'albarà, el document adjunt i les dades OCR
     * temporals per actualitzar el registre seleccionat.
     *
     * @param id identificador de l'albarà de proveïdor
     * @param albaraProveidor dades actualitzades de l'albarà
     * @param imatgeAlbara document adjunt de l'albarà
     * @param ocrDocumentTemporalId identificador del document OCR temporal
     * @param session sessió web de l'usuari
     * @param model model de dades de la vista
     * @return redirecció al llistat o vista del formulari amb errors
     */
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


    /**
     * ELIMINACIÓ D'UN ALBARÀ DE PROVEÏDOR.
     *
     * Sol·licitada l'eliminació de l'albarà seleccionat i preparat el model
     * amb l'error corresponent quan l'operació no és possible.
     *
     * @param id identificador de l'albarà de proveïdor
     * @param model model de dades de la vista
     * @return redirecció al llistat o vista de detall amb error
     */
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


    /**
     * CÀRREGA DE DADES DEL FORMULARI.
     *
     * Afegides al model les dades comunes necessàries per emplenar
     * un formulari d'albarà de proveïdor.
     *
     * @param model model de dades de la vista
     */
    private void carregarDadesFormulari(Model model) {
        model.addAttribute("proveidors", proveidorService.getAllProveidors());
        model.addAttribute("materiesPrimeres", materiaPrimeraService.getAllMateriesPrimeres());
        model.addAttribute("unitatsMesura", unitatMesuraService.getAllUnitatsMesura());
    }


    /**
     * PREVISUALITZACIÓ DEL DOCUMENT OCR.
     *
     * Afegides al model les dades disponibles per mostrar el document OCR
     * temporal sense tornar a carregar el fitxer original.
     *
     * @param model model de dades de la vista
     * @param ocrDocumentTemporalId identificador del document OCR temporal
     */
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


    /**
     * OBTENCIÓ DEL RESULTAT OCR TEMPORAL.
     *
     * Recuperat el resultat OCR mantingut temporalment a la sessió
     * durant l'edició de l'albarà.
     *
     * @param session sessió web de l'usuari
     * @return resultat OCR temporal o valor nul si no existeix
     */
    private OcrResultatAlbaraProveidorDto obtenirResultatOcrSessio(HttpSession session) {
        Object resultat = session.getAttribute(SESSION_OCR_RESULTAT);
        return resultat instanceof OcrResultatAlbaraProveidorDto
                ? (OcrResultatAlbaraProveidorDto) resultat
                : null;
    }


    /**
     * OBTENCIÓ DE L'IDENTIFICADOR OCR.
     *
     * Recuperada la referència al document OCR temporal a partir del valor rebut
     * o del resultat emmagatzemat a la sessió.
     *
     * @param ocrDocumentTemporalId identificador rebut des del formulari
     * @param resultatOcr resultat OCR temporal disponible
     * @return identificador del document OCR temporal o valor nul
     */
    private String obtenirDocumentTemporalId(String ocrDocumentTemporalId, OcrResultatAlbaraProveidorDto resultatOcr) {
        if (ocrDocumentTemporalId != null && !ocrDocumentTemporalId.isBlank()) {
            return ocrDocumentTemporalId;
        }

        return resultatOcr != null ? resultatOcr.getOcrDocumentTemporalId() : null;
    }


    /**
     * PREPARACIÓ DE LES LÍNIES DEL FORMULARI.
     *
     * Garantida l'existència d'una línia inicial quan l'albarà rebut
     * no conté lots per mostrar al formulari.
     *
     * @param albaraProveidor albarà que s'ha de mostrar al formulari
     */
    private void assegurarLotsFormulari(AlbaraProveidor albaraProveidor) {
        if (albaraProveidor.getLots() == null || albaraProveidor.getLots().isEmpty()) {
            albaraProveidor.setLots(new ArrayList<>(List.of(new LotProveidor())));
        }
    }


    /**
     * OBTENCIÓ D'UN MISSATGE TRADUÏT.
     *
     * Recuperat el text corresponent al codi indicat segons l'idioma
     * actiu de la interfície web.
     *
     * @param codi codi del missatge que s'ha de recuperar
     * @param arguments valors incorporats al missatge
     * @return missatge traduït a l'idioma actiu
     */
    private String missatge(String codi, Object... arguments) {
        return messageSource.getMessage(codi, arguments, LocaleContextHolder.getLocale());
    }

}
