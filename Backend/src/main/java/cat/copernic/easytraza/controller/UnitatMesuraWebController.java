package cat.copernic.easytraza.controller;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import cat.copernic.easytraza.dto.OcrResultatAlbaraProveidorDto;
import cat.copernic.easytraza.entities.AlbaraProveidor;
import cat.copernic.easytraza.entities.LotProveidor;
import cat.copernic.easytraza.entities.UnitatMesura;
import cat.copernic.easytraza.service.MateriaPrimeraService;
import cat.copernic.easytraza.service.OcrAlbaraProveidorService;
import cat.copernic.easytraza.service.ProveidorService;
import cat.copernic.easytraza.service.UnitatMesuraService;
import jakarta.servlet.http.HttpSession;

/**
 * CONTROLADOR WEB D'UNITATS DE MESURA.
 *
 * Gestionada la creació d'unitats de mesura des del formulari d'albarans
 * de proveïdor, conservant les dades temporals del procés OCR.
 *
 * @author Ángel Jurado Herruz
 */
@Controller
@RequestMapping("/unitats-mesura")
public class UnitatMesuraWebController {

    private static final Logger LOGGER = LoggerFactory.getLogger(UnitatMesuraWebController.class);

    private static final String SESSION_OCR_RESULTAT = "ocrResultatAlbaraProveidor";

    // ---------------------------- SERVICES I CONSTRUCTOR ----------------------------
    private final UnitatMesuraService unitatMesuraService;
    private final ProveidorService proveidorService;
    private final MateriaPrimeraService materiaPrimeraService;
    private final OcrAlbaraProveidorService ocrAlbaraProveidorService;
    private final MessageSource messageSource;

    public UnitatMesuraWebController(UnitatMesuraService unitatMesuraService,
                                     ProveidorService proveidorService,
                                     MateriaPrimeraService materiaPrimeraService,
                                     OcrAlbaraProveidorService ocrAlbaraProveidorService,
                                     MessageSource messageSource) {
        this.unitatMesuraService = unitatMesuraService;
        this.proveidorService = proveidorService;
        this.materiaPrimeraService = materiaPrimeraService;
        this.ocrAlbaraProveidorService = ocrAlbaraProveidorService;
        this.messageSource = messageSource;
    }


    /**
     * GUARDAT D'UNA UNITAT DE MESURA.
     *
     * Creada una unitat de mesura des del formulari d'albarà de proveïdor
     * i conservades les dades ja introduïdes i la informació OCR temporal.
     *
     * @param albaraProveidor dades actuals de l'albarà introduïdes al formulari
     * @param novaUnitatMesura nom de la unitat de mesura que s'ha de crear
     * @param indexUnitatMesura posició del lot al qual s'ha d'assignar la unitat
     * @param ocrDocumentTemporalId identificador del document OCR temporal
     * @param session sessió web de l'usuari
     * @param model model de dades de la vista
     * @return vista del formulari d'albarans de proveïdor
     */
    @PostMapping("/save-from-albara-proveidor")
    public String guardarUnitatMesuraDesAlbaraProveidor(
            @ModelAttribute("albaraProveidor") AlbaraProveidor albaraProveidor,
            @RequestParam(value = "novaUnitatMesura", required = false) String novaUnitatMesura,
            @RequestParam(value = "indexUnitatMesura", required = false) Integer indexUnitatMesura,
            @RequestParam(value = "ocrDocumentTemporalId", required = false) String ocrDocumentTemporalId,
            HttpSession session,
            Model model) {

        // Assegura que el formulari sempre tingui almenys un lot
        if (albaraProveidor.getLots() == null || albaraProveidor.getLots().isEmpty()) {
            List<LotProveidor> lots = new ArrayList<>();
            lots.add(new LotProveidor());
            albaraProveidor.setLots(lots);
        }

        try {
            UnitatMesura unitatMesura = unitatMesuraService.createUnitatMesura(novaUnitatMesura);
            LOGGER.info("Unitat de mesura creada correctament.");

            if (indexUnitatMesura != null
                    && indexUnitatMesura >= 0
                    && indexUnitatMesura < albaraProveidor.getLots().size()) {

                albaraProveidor.getLots().get(indexUnitatMesura).setUnitats(unitatMesura.getNom());
            }

            model.addAttribute("infoUnitatMesura", missatge("unitatsMesura.missatge.creada"));
        }
        catch (RuntimeException e) {
            LOGGER.warn("No s'ha pogut crear la unitat de mesura des de l'albarà de proveïdor: {}", e.getMessage());
            model.addAttribute("errorUnitatMesura", e.getMessage());
            model.addAttribute("novaUnitatMesuraValor", novaUnitatMesura);
        }

        OcrResultatAlbaraProveidorDto resultatOcr = obtenirResultatOcrSessio(session);

        if ((ocrDocumentTemporalId == null || ocrDocumentTemporalId.isBlank()) && resultatOcr != null) {
            ocrDocumentTemporalId = resultatOcr.getOcrDocumentTemporalId();
        }

        model.addAttribute("unitatMesuraPanelObert", true);
        model.addAttribute("unitatMesuraPanelIndex", indexUnitatMesura);
        model.addAttribute("albaraProveidor", albaraProveidor);
        model.addAttribute("ocrResultat", resultatOcr);
        model.addAttribute("ocrDocumentTemporalId", ocrDocumentTemporalId);
        afegirDadesDocumentTemporal(model, ocrDocumentTemporalId);
        model.addAttribute("proveidors", proveidorService.getAllProveidors());
        model.addAttribute("materiesPrimeres", materiaPrimeraService.getAllMateriesPrimeres());
        model.addAttribute("unitatsMesura", unitatMesuraService.getAllUnitatsMesura());

        return "albaransProveidor/formAlbaraProveidor";
    }


    /**
     * OBTENCIÓ DEL RESULTAT OCR TEMPORAL.
     *
     * Recuperat el resultat OCR mantingut temporalment a la sessió
     * durant l'edició de l'albarà de proveïdor.
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
     * PREVISUALITZACIÓ DEL DOCUMENT OCR.
     *
     * Afegides al model les dades disponibles per mostrar el document OCR
     * temporal sense tornar a carregar la imatge original.
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
