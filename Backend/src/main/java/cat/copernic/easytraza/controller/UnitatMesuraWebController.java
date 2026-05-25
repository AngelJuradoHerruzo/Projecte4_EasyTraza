package cat.copernic.easytraza.controller;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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

    public UnitatMesuraWebController(UnitatMesuraService unitatMesuraService,
                                     ProveidorService proveidorService,
                                     MateriaPrimeraService materiaPrimeraService,
                                     OcrAlbaraProveidorService ocrAlbaraProveidorService) {
        this.unitatMesuraService = unitatMesuraService;
        this.proveidorService = proveidorService;
        this.materiaPrimeraService = materiaPrimeraService;
        this.ocrAlbaraProveidorService = ocrAlbaraProveidorService;
    }


    // GUARDAR UNITAT DE MESURA DES DEL FORMULARI D'ALBARÀ DE PROVEÏDOR
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

            model.addAttribute("infoUnitatMesura", "Unitat de mesura creada correctament.");
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


    // OBTENIR RESULTAT OCR TEMPORAL CONSERVAT A LA SESSIÓ
    private OcrResultatAlbaraProveidorDto obtenirResultatOcrSessio(HttpSession session) {
        Object resultat = session.getAttribute(SESSION_OCR_RESULTAT);
        return resultat instanceof OcrResultatAlbaraProveidorDto
                ? (OcrResultatAlbaraProveidorDto) resultat
                : null;
    }


    // RECUPERAR VISTA PRÈVIA DEL DOCUMENT OCR SENSE REENVIAR LA IMATGE
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
}
